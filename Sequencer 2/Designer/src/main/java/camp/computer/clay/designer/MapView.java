package camp.computer.clay.designer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

import camp.computer.clay.sprite.BoardSprite;
import camp.computer.clay.sprite.PortScopeSprite;
import camp.computer.clay.sprite.Sprite;
import camp.computer.clay.sprite.SystemSprite;
import camp.computer.clay.sprite.util.Geometry;

public class MapView extends SurfaceView implements SurfaceHolder.Callback {

    private MapViewRenderer mapViewRenderer;

    private SurfaceHolder surfaceHolder;

    // Canvas
    private Bitmap canvasBitmap = null;
    private Canvas mapCanvas = null;
    private int canvasWidth, canvasHeight;
    private Paint paint = new Paint (Paint.ANTI_ALIAS_FLAG);
    private Matrix identityMatrix;

    // Map
    private PointF originPosition = new PointF ();
    private PointF currentPosition = new PointF ();

    ArrayList<SystemSprite> systemSprites = new ArrayList<SystemSprite>();

    public MapView(Context context) {
        super(context);
        initialize();
    }

    public MapView (Context context, AttributeSet attrs) {
        super (context, attrs);
        initialize();
    }

    public MapView (Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        initialize();
    }

    private void initialize() {
        initializeSprites();
        setUpTouchProcessor();
    }

    public void initializeSprites() {
        systemSprites.add(new SystemSprite());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        canvasWidth = getWidth ();
        canvasHeight = getHeight();
        canvasBitmap = Bitmap.createBitmap (canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
        mapCanvas = new Canvas ();
        mapCanvas.setBitmap(canvasBitmap);


        // TODO: Move setPosition to a better location!
//        getClay().getPerspective ().setPosition(mapCanvas.getWidth() / 2, mapCanvas.getHeight() / 2);
        originPosition.set(mapCanvas.getWidth() / 2.0f, mapCanvas.getHeight() / 2.0f);

        currentPosition.set(originPosition.x, originPosition.y);

        identityMatrix = new Matrix ();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void MapView_OnResume () {
        Log.v("MapView", "MapView_OnResume");

        surfaceHolder = getHolder ();
        getHolder ().addCallback (this);

        // Create and start background Thread
        mapViewRenderer = new MapViewRenderer(this);
        mapViewRenderer.setRunning (true);
        mapViewRenderer.start ();

//        // Start communications
//        getClay ().getCommunication ().startDatagramServer();

        updateSurfaceView();

    }

    public void MapView_OnPause () {
        Log.v("MapView", "MapView_OnPause");

        // Pause the communications
//        getClay ().getCommunication ().stopDatagramServer (); // HACK: This was commented out to prevent the server from "crashing" into an invalid state!

        // Kill the background Thread
        boolean retry = true;
        mapViewRenderer.setRunning (false);

        while (retry) {
            try {
                mapViewRenderer.join ();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    }

    void drawLabel () {

        paint.setStyle (Paint.Style.FILL);
        paint.setStrokeWidth (0);
        paint.setColor (Color.BLACK);
        paint.setTextSize (80);

        // Set style for behavior's label
        String title = "Clay";

        Rect textBounds = new Rect ();
        paint.getTextBounds (title, 0, title.length (), textBounds);
        //mapCanvas.drawText (title, 0 - (textBounds.width () / 2), 0 - (textBounds.height () / 2), paint);

        mapCanvas.drawText (title, 50, 50, paint);

    }

    void drawSquare (float xCenter, float yCenter, float angle) {

        float width = 100;
        float height = 100;

        mapCanvas.save();

        // Position
        // Reference: http://stackoverflow.com/questions/8712652/rotating-image-on-a-canvas-in-android
//        mapCanvas.translate(xCenter, yCenter);
//        mapCanvas.rotate(angle);
        mapCanvas.rotate(angle, xCenter, yCenter);

        // Fill
        paint.setStyle (Paint.Style.FILL);
        paint.setColor (Color.LTGRAY);
        mapCanvas.drawRect(xCenter - (width / 2.0f), yCenter - (height / 2.0f), xCenter + (width / 2.0f), yCenter + (height / 2.0f), paint);

        // Stroke
        paint.setStyle (Paint.Style.STROKE);
        paint.setStrokeWidth (3);
        paint.setColor (Color.BLACK);
        mapCanvas.drawRect(xCenter - (width / 2.0f), yCenter - (height / 2.0f), xCenter + (width / 2.0f), yCenter + (height / 2.0f), paint);

        mapCanvas.restore();

    }

    private void drawSprite(Sprite sprite) {
        sprite.draw(mapCanvas, paint);
    }

    //----------------------------------------------------------------------------------------------
    // Coordinate System
    //----------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------
    // Perspective
    //----------------------------------------------------------------------------------------------

    public static float DEFAULT_SCALE_FACTOR = 1.0f;

    //private Point originPosition = new Point (0, 0);
    private float scale = DEFAULT_SCALE_FACTOR;

    //----------------------------------------------------------------------------------------------
    // Layout
    //----------------------------------------------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Move the perspective
        mapCanvas.save ();
        //mapCanvas.translate (originPosition.x, originPosition.y);
        mapCanvas.translate (currentPosition.x, currentPosition.y);
        mapCanvas.scale (scale, scale);
//        mapCanvas.translate (getClay ().getPerspective ().getPosition ().x, getClay ().getPerspective ().getPosition ().y);
//        mapCanvas.scale (getClay ().getPerspective ().getScaleFactor (), getClay ().getPerspective ().getScaleFactor ());

        // Draw the background
        mapCanvas.drawColor (Color.WHITE);

        // Scene
        drawScene();

        // Paint the bitmap to the "primary" canvas.
        canvas.drawBitmap (canvasBitmap, identityMatrix, null);

        mapCanvas.restore();
    }

    @SuppressLint("WrongCall")
    public void updateSurfaceView () {
        // The function run in background thread, not UI thread.

        Canvas canvas = null;

        try {
            canvas = surfaceHolder.lockCanvas ();

            synchronized (surfaceHolder) {
                updateState();
                if (canvas != null) {
                    onDraw(canvas);
                }
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost (canvas);
            }
        }
    }

    private void updateState() {
        if (!hasTouches()) {
            for (SystemSprite systemSprite : systemSprites) {
                systemSprite.updateState();
            }
        }
    }

    private void drawScene () {
        // drawTitle();

        for (SystemSprite systemSprite : systemSprites) {
            drawSprite(systemSprite);
        }
    }

    //----------------------------------------------------------------------------------------------
    // Human Interaction Model
    //----------------------------------------------------------------------------------------------

    public static int MAXIMUM_TOUCH_POINT_COUNT = 5;

    public static int MAXIMUM_TAP_DURATION = 200;
    public static int MAXIMUM_DOUBLE_TAP_DURATION = 400;
    public static int MINIMUM_HOLD_DURATION = 800;

    public static int MINIMUM_DRAG_DISTANCE = 35;

    private PointF[] touch = new PointF[MAXIMUM_TOUCH_POINT_COUNT];
    private long[] touchTime = new long[MAXIMUM_TOUCH_POINT_COUNT];
    private boolean[] isTouching = new boolean[MAXIMUM_TOUCH_POINT_COUNT];
    private boolean[] isDragging = new boolean[MAXIMUM_TOUCH_POINT_COUNT];
    private double[] dragDistance = new double[MAXIMUM_TOUCH_POINT_COUNT];

    private PointF[] touchPrevious = new PointF[MAXIMUM_TOUCH_POINT_COUNT];
    private long[] touchPreviousTime = new long[MAXIMUM_TOUCH_POINT_COUNT];
    private boolean[] isTouchingPrevious = new boolean[MAXIMUM_TOUCH_POINT_COUNT];
    private boolean[] isTouchingActionPrevious = new boolean[MAXIMUM_TOUCH_POINT_COUNT];

    // Point where the touch started.
    private PointF[] touchStart = new PointF[MAXIMUM_TOUCH_POINT_COUNT];
    private long touchStartTime = java.lang.System.currentTimeMillis ();

    // Point where the touch ended.
    private PointF[] touchStop = new PointF[MAXIMUM_TOUCH_POINT_COUNT];
    private long touchStopTime = java.lang.System.currentTimeMillis ();

    // Touch state
    private boolean hasTouches = false; // i.e., At least one touch is detected.
    private int touchCount = 0; // i.e., The total number of touch points detected.
    private boolean[] isTouchingSprite = new boolean[MAXIMUM_TOUCH_POINT_COUNT];
    private BoardSprite[] touchedSprite = new BoardSprite[MAXIMUM_TOUCH_POINT_COUNT];

    // Interactivity state
    private boolean isPanningDisabled = false;

    // Gesture Envelope for Making a Wireless Channel
    // Gestural language. Grammar for the gestures composing it. Think of these as templates for
    // gestures that Clay attempts to evaluate and cleans up after, following each touch action.
    private BoardSprite sourceBoardSprite = null;
    private int sourceChannelScopeIndex = -1;
    private BoardSprite destinationBoardSprite = null;
    private int destinationChannelScopeIndex = -1;

    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            /* do what you need to do */
            //foobar();
            int pointerId = 0;
            if (isTouching[pointerId])
                if (dragDistance[pointerId] < MINIMUM_DRAG_DISTANCE) {
                    onHoldCallback(pointerId);
                }

            // Uncomment this for periodic callback
            // timerHandler.postDelayed(this, 100);
        }
    };

    private void setUpTouchProcessor () {
        for (int i = 0; i < MAXIMUM_TOUCH_POINT_COUNT; i++) {
            touch[i] = new PointF();
            touchPrevious[i] = new PointF();
            touchStart[i] = new PointF();
            touchStop[i] = new PointF();
        }
    }

    public boolean hasTouches () {
        for (int i = 0; i < MAXIMUM_TOUCH_POINT_COUNT; i++) {
            if (isTouching[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent (MotionEvent motionEvent) {

        int pointerIndex = ((motionEvent.getAction () & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT);
        int pointerId = motionEvent.getPointerId (pointerIndex);
        int touchAction = (motionEvent.getAction () & MotionEvent.ACTION_MASK);
        int pointCount = motionEvent.getPointerCount ();

        if (pointCount <= MAXIMUM_TOUCH_POINT_COUNT) {
            if (pointerIndex <= MAXIMUM_TOUCH_POINT_COUNT - 1) {

                // Current
                // Update touch state based the points given by the host OS (e.g., Android).
                for (int i = 0; i < pointCount; i++) {
                    int id = motionEvent.getPointerId (i);
                    touch[id].x = (motionEvent.getX (i) - currentPosition.x) / scale;
                    touch[id].y = (motionEvent.getY (i) - currentPosition.y) / scale;
                    touchTime[id] = java.lang.System.currentTimeMillis ();

//                    xTouch[id] = (motionEvent.getX (i) - currentPosition.x) / scale + mapCanvas.getClipBounds().left;
//                    yTouch[id] = (motionEvent.getY (i) - currentPosition.y) / scale + mapCanvas.getClipBounds().top;
                }

                // Update the state of the touched object based on the current touch interaction state.
                if (touchAction == MotionEvent.ACTION_DOWN) {
                    onTouchCallback(pointerId);
                } else if (touchAction == MotionEvent.ACTION_POINTER_DOWN) {
                    // TODO:
                } else if (touchAction == MotionEvent.ACTION_MOVE) {
                    onMoveCallback(pointerId);
                } else if (touchAction == MotionEvent.ACTION_UP) {
                   onReleaseCallback(pointerId);
                } else if (touchAction == MotionEvent.ACTION_POINTER_UP) {
                    // TODO:
                } else if (touchAction == MotionEvent.ACTION_CANCEL) {
                    // TODO:
                } else {
                    // TODO:
                }
            }
        }

        return true;
    }

    private void onTouchCallback (int pointerId) {
        Log.v("MapViewEvent", "onTouchCallback");

        // Previous
        isTouchingPrevious[pointerId] = isTouching[pointerId]; // (or) isTouchingPrevious[pointerId] = false;
        touchPrevious[pointerId].x = touch[pointerId].x;
        touchPrevious[pointerId].y = touch[pointerId].y;
        touchPreviousTime[pointerId] = java.lang.System.currentTimeMillis ();

        // Current
        isTouching[pointerId] = true;

        // First
        if (this.isTouching[pointerId] == true && this.isTouchingPrevious[pointerId] == false) {

            // Set the first point of touch
            this.touchStart[pointerId].x = this.touch[pointerId].x;
            this.touchStart[pointerId].y = this.touch[pointerId].y;
            this.touchStartTime = java.lang.System.currentTimeMillis ();

            // Reset dragging state
            this.isDragging[pointerId] = false;
            this.dragDistance[pointerId] = 0;

            // Reset object interaction state
            for (SystemSprite systemSprite : systemSprites) {
                for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {
                    // Log.v ("MapViewTouch", "Object at " + boardSprite.x + ", " + boardSprite.y);

                    // Check if one of the objects is touched
                    if (boardSprite.isTouching(touchStart[pointerId])) {

                        // TODO: Add this to an onTouch callback for the sprite's channel nodes
                        // TODO: i.e., callback Sprite.onTouch (via Sprite.touch())

                        this.isTouchingSprite[pointerId] = true;
                        this.touchedSprite[pointerId] = boardSprite;

                        isPanningDisabled = true;

                        // Break to limit the number of objects that can be touch by a finger to one (1:1 finger:touch relationship).
                        break;

                    }

//                            // Start touch on a channel scope
//                            if (boardSprite.showFormLayer) {
//
//                                if (sourceChannelScopeIndex == -1) {
//
//                                    /*
//                                    // TODO: Add this to an onTouch callback for the sprite's channel nodes
//                                    // Check if the touched board's I/O node is touched
//                                    for (int i = 0; i < boardSprite.channelScopePositions.size(); i++) {
//                                        PointF channelNodePoint = boardSprite.channelScopePositions.get(i);
//                                        // Check if one of the objects is touched
//                                        if (getDistance((int) channelNodePoint.x, (int) channelNodePoint.y, (int) xTouchStart[pointerId], (int) yTouchStart[pointerId]) < 60) {
//                                            Log.v("MapViewTouch", "touched node " + (i + 1));
//                                            sourceChannelScopeIndex = i;
//                                            boardSprite.channelTypes.set(
//                                                    i,
//                                                    BoardSprite.ChannelType.getNextType(boardSprite.channelTypes.get(i)) // (boardSprite.channelTypes.get(i) + 1) % boardSprite.channelTypeColors.length
//                                            );
//                                        }
//                                    }
//                                    */
//
//                                }
//
//                            }

                }
            }

//                        if (touchedSprite == false) {
//                            if ()
//                            sourceBoardSprite = null;
//                        }

            // Touch the canvas
            if (this.touchedSprite[pointerId] == null) {
                this.isTouchingSprite[pointerId] = false;
                this.isPanningDisabled = false;
            }

            // Start timer to check for hold
            timerHandler.removeCallbacks(timerRunnable);
            timerHandler.postDelayed(timerRunnable, MINIMUM_HOLD_DURATION);

//                        // Move the touched object to be the top object layer
//                        if (sourceBoardSprite != null) {
//                            boardSprites.remove(sourceBoardSprite);
//                            boardSprites.add(sourceBoardSprite);
//                        }

        }
    }

    private void onTapCallback (int pointerId) {

    }

    private void onDoubleTapCallback (int pointerId) {

    }

    private void onHoldCallback (int pointerId) {
        Log.v("MapViewEvent", "onHoldCallback");

        if (dragDistance[pointerId] < this.MINIMUM_DRAG_DISTANCE) {
            // Holding but not (yet) dragging.

            // Disable panning
            isPanningDisabled = false;

            // Hide scopes
            if (sourceChannelScopeIndex == -1) {
                for (SystemSprite systemSprite : this.systemSprites) {
                    for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {
                        boardSprite.hideChannelScopes();
                        boardSprite.hideChannelPaths();
                    }
                }
            }

            // Show scope for source board
            if (touchedSprite[pointerId] != null) {
                sourceBoardSprite = touchedSprite[pointerId];
                sourceBoardSprite.showChannelScopes();
                sourceBoardSprite.showChannelPaths();
            }

        }
    }

    private void onMoveCallback (int pointerId) {
        Log.v("MapViewEvent", "onMoveCallback");

        // Previous
        isTouchingPrevious[pointerId] = isTouching[pointerId];
        touchPrevious[pointerId].x = touch[pointerId].x;
        touchPrevious[pointerId].y = touch[pointerId].y;

//                    // Current
//                    isTouching[pointerId] = true;
//                    xTouch[pointerId] = motionEvent.getX (i);
//                    yTouch[pointerId] = motionEvent.getY (i);

        // Calculate drag distance
        dragDistance[pointerId] = Geometry.getDistance(touch[pointerId], touchStart[pointerId]);

        // Classify/Callback
        if (dragDistance[pointerId] < this.MINIMUM_DRAG_DISTANCE) {
            // Pre-dragging
            onPreDragCallback(pointerId);
        } else {
            // Dragging
            this.isDragging[pointerId] = true;
            onDragCallback(pointerId);
        }
    }

    private void onPreDragCallback (int pointerId) {

    }

    private void onDragCallback (int pointerId) {
        Log.v("MapViewEvent", "onDragCallback");

        // Process
        // TODO: Put into callback

        // Dragging and holding.
        if (touchTime[pointerId] - touchStartTime < MINIMUM_HOLD_DURATION) {

            // Dragging only (not holding)

            Log.v("MapViewEvent", "\tB");

            // TODO: Put into callback
            if (!isPanningDisabled) {
                currentPosition.offset((int) (touch[pointerId].x - touchStart[pointerId].x), (int) (touch[pointerId].y - touchStart[pointerId].y));
            } else {
//                if (sourceBoardSprite != null) {
                    if (this.isTouchingSprite[pointerId]) {
                        //sourceBoardSprite.scale = 1.3f;
                        touchedSprite[pointerId].showHighlights = true;
                        touchedSprite[pointerId].setPosition(touch[pointerId].x, touch[pointerId].y);
                    }
//                }
            }

        } else {
            Log.v("MapViewEvent", "\tA");

            // Holding and dragging.

            // TODO: Check if (1) drag through a channel connector node, then if (2) drag to another board, then (3) set up communication channel (or abandon if not all steps done)
            if (sourceBoardSprite != null) {
                Log.v("MapViewEvent", "\tA2");

                // Start touch on a channel scope
                if (sourceChannelScopeIndex == -1) {

                    if (sourceBoardSprite != null) {
                        // If no channel source has been touched yet, check if one is dragged over.

                        // TODO: Add this to an onTouch callback for the sprite's channel nodes
                        // Check if the touched board's I/O node is touched
                        for (int i = 0; i < sourceBoardSprite.getChannelCount(); i++) {
                            if (sourceBoardSprite.portScopeSprites.get(i).showFormLayer) {
                                // Check if one of the objects is touched
                                if (Geometry.getDistance(touch[pointerId], sourceBoardSprite.portScopeSprites.get(i).getPosition()) < 60) {
                                    Log.v("MapViewTouch", "touched node " + (i + 1));
                                    sourceChannelScopeIndex = i;
                                    sourceBoardSprite.portScopeSprites.get(i).channelType = PortScopeSprite.ChannelType.getNextType(sourceBoardSprite.portScopeSprites.get(i).channelType); // (boardSprite.channelTypes.get(i) + 1) % boardSprite.channelTypeColors.length
                                }
                            }

                        }
                    }
                } else if (destinationBoardSprite == null) {

                    if (sourceChannelScopeIndex >= 0) {
                        Log.v("MapViewTouch", "\tLooking for destination");

                        // Check if a board was touched
                        for (SystemSprite systemSprite : this.systemSprites) {
                            for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {
                                // Log.v ("MapViewTouch", "Object at " + boardSprite.x + ", " + boardSprite.y);

                                // TODO: Add this to an isTouch? function of the sprite object
                                // Check if one of the objects is touched
                                if (Geometry.getDistance(touch[pointerId], boardSprite.getPosition()) < (boardSprite.boardWidth / 3.0f)) {

                                    // TODO: Add this to an onTouch callback for the sprite's channel nodes

                                    Log.v("MapViewTouch", "\tTouching object at " + boardSprite.getPosition().x + ", " + boardSprite.getPosition().y);
                                    //this.isTouchingSprite[pointerId] = true;
                                    destinationBoardSprite = boardSprite;

                                    destinationBoardSprite.showChannelScopes();

                                    // TODO: Callback: call Sprite.onTouchDestination (via Sprite.touch())
                                }
                            }
                        }

                    }
                } else if (destinationChannelScopeIndex == -1) {
                    Log.v("MapViewTouch", "\tLooking for destination SCOPE");
                    if (destinationBoardSprite != null) {
                        // If no channel source has been touched yet, check if one is dragged over.

                        // TODO: Add this to an onTouch callback for the sprite's channel nodes
                        // Check if the touched board's I/O node is touched
                        for (int i = 0; i < destinationBoardSprite.getChannelCount(); i++) {
                            if (destinationBoardSprite.portScopeSprites.get(i).showFormLayer) {
                                // Check if one of the objects is touched
                                if (Geometry.getDistance(touch[pointerId], destinationBoardSprite.portScopeSprites.get(i).getPosition()) < 60.0f) {
                                    Log.v("MapViewTouch", "touched node " + (i + 1));
                                    destinationChannelScopeIndex = i;
                                    destinationBoardSprite.portScopeSprites.get(i).channelType = PortScopeSprite.ChannelType.getNextType(destinationBoardSprite.portScopeSprites.get(i).channelType); // (boardSprite.channelTypes.get(i) + 1) % boardSprite.channelTypeColors.length
                                }
                            }

                        }
                    }

                }

            } else {

                // TODO: Put into callback
                 if (this.isTouchingSprite[pointerId]) {
                    //sourceBoardSprite.scale = 1.3f;
                    touchedSprite[pointerId].showHighlights = true;
                    touchedSprite[pointerId].setPosition(touch[pointerId].x, touch[pointerId].y);
                } else if (!isPanningDisabled) {
                     currentPosition.offset((int) (touch[pointerId].x - touchStart[pointerId].x), (int) (touch[pointerId].y - touchStart[pointerId].y));
                 }
            }

        }
    }

    private void onReleaseCallback (int pointerId) {
        Log.v("MapViewEvent", "onReleaseCallback");

        // Previous
        isTouchingPrevious[pointerId] = isTouching[pointerId];
        touchPrevious[pointerId].x = touch[pointerId].x;
        touchPrevious[pointerId].y = touch[pointerId].y;

        // Current
        isTouching[pointerId] = false;

        // Stop touching. Check if this is the start of a touch gesture (i.e., the first touch in a sequence of touch events for the given finger)
        if (this.isTouching[pointerId] == false && this.isTouchingPrevious[pointerId] == true) {
            this.touchStop[pointerId].x = this.touch[pointerId].x;
            this.touchStop[pointerId].y = this.touch[pointerId].y;
            this.touchStopTime = java.lang.System.currentTimeMillis ();
        }

        boolean isGestureInProgress = false;

        // Classify/Callbacks
        if (touchStopTime - touchStartTime < MAXIMUM_TAP_DURATION) {

            // Step 1: Touch source board
            if (/*sourceBoardSprite == null && */ sourceChannelScopeIndex == -1
                    && destinationBoardSprite == null && destinationChannelScopeIndex == -1) {
                Log.v("MapView", "Looking for source board touch.");

                // Hide channel scopes (unless dragging)

                for (SystemSprite systemSprite : this.systemSprites) {
                    for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {

                        // TODO: Add this to an onTouch callback for the sprite's channel nodes
                        // Check if the touched board's I/O node is touched
                        // Check if one of the objects is touched
                        if (Geometry.getDistance(touchStart[pointerId], boardSprite.getPosition()) < 80) {
                            Log.v("MapView", "\tSource board touched.");

                            sourceBoardSprite = boardSprite;

//                                        if (sourceBoardSprite.showFormLayer) {
//                                            // Touched board that's showing channel scopes.
//                                            sourceBoardSprite.showFormLayer = false;
//                                            sourceBoardSprite.showChannelPaths = false;
//                                            sourceBoardSprite = null;
//
//                                            // Reset style and visualization.
//                                            for (BoardSprite boardSprite2 : this.boardSprites) {
//                                                boardSprite2.showFormLayer = false;
//                                                boardSprite2.setTransparency(1.0f);
//                                            }
//
//                                            ApplicationView.getApplicationView().speakPhrase("stopping");
//                                        } else {
                            // No touch on board or scope. Touch is on map. So hide scopes.
                            for (SystemSprite systemSprite2 : this.systemSprites) {
                                for (BoardSprite boardSprite2 : systemSprite2.getBoardSprites()) {
                                    boardSprite2.hideChannelScopes();
                                    boardSprite2.hideChannelPaths();
                                    boardSprite2.setTransparency(0.2f);
                                }
                            }
                            sourceBoardSprite.showChannelScopes();
                            sourceBoardSprite.showChannelPaths();
                            sourceBoardSprite.setTransparency(1.0f);
                            ApplicationView.getApplicationView().speakPhrase("choose a channel to get data.");
//                                        }

                            isGestureInProgress = true;

                            break;
                        }
                    }
                }
            }

            // Step 2: Touch source channel scope
            if (!isGestureInProgress) {
                if (sourceBoardSprite != null /* && sourceChannelScopeIndex == -1 */
                        && destinationBoardSprite == null && destinationChannelScopeIndex == -1) {
                    Log.v("MapView", "Looking for source channel scope touch.");

                    if (!isDragging[pointerId]) {

                        for (SystemSprite systemSprite : this.systemSprites) {
                            for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {

                                // Check if the touched board's chanenl scope is touched
                                for (int scopeIndex = 0; scopeIndex < boardSprite.getChannelCount(); scopeIndex++) {
                                    // Check if one of the objects is touched
                                    // TODO: Create BoardChannelSprite.isTouching()
                                    if (Geometry.getDistance(touchStart[pointerId], boardSprite.portScopeSprites.get(scopeIndex).getPosition()) < 80) {
                                        if (boardSprite == sourceBoardSprite) {

                                            if (sourceChannelScopeIndex == -1) {

                                                // First touch on the source channel scope

                                                if (boardSprite.portScopeSprites.get(scopeIndex).channelType == PortScopeSprite.ChannelType.NONE) {

                                                    Log.v("MapView", "\tSource channel scope " + (scopeIndex + 1) + " touched.");
                                                    sourceChannelScopeIndex = scopeIndex;
                                                    boardSprite.portScopeSprites.get(scopeIndex).channelType = PortScopeSprite.ChannelType.getNextType(boardSprite.portScopeSprites.get(scopeIndex).channelType); // (boardSprite.channelTypes.get(i) + 1) % boardSprite.channelTypeColors.length

                                                    ApplicationView.getApplicationView().speakPhrase("setting as input. you can send the data to another board if you want. touch another board.");

                                                    isGestureInProgress = true;

                                                    break;

                                                } else {

                                                    // TODO: If second press, change the channel.

                                                    Log.v("MapView", "\tSource channel scope " + (scopeIndex + 1) + " touched.");
                                                /*
                                                sourceChannelScopeIndex = scopeIndex;
                                                boardSprite.portScopeSprites.get(scopeIndex).channelType = PortScopeSprite.ChannelType.getNextType(boardSprite.portScopeSprites.get(scopeIndex).channelType); // (boardSprite.channelTypes.get(i) + 1) % boardSprite.channelTypeColors.length
                                                */

                                                    for (SystemSprite systemSprite2 : this.systemSprites) {
                                                        for (BoardSprite boardSprite2 : systemSprite2.getBoardSprites()) {
                                                            boardSprite2.hideChannelScopes();
                                                            boardSprite2.hideChannelPaths();
                                                        }
                                                    }
                                                    boardSprite.showChannelScope(scopeIndex);
                                                    boardSprite.showChannelPath(scopeIndex, true);
//                                                boardSprite.showChannelPaths

                                                    ApplicationView.getApplicationView().speakPhrase("setting as input. you can send the data to another board if you want. touch another board.");

                                                    isGestureInProgress = true;

                                                    break;

                                                }

                                            } else {

                                                // TODO: Create BoardChannelSprite.isTouching()
                                                if (sourceChannelScopeIndex == scopeIndex) {
                                                    // Touched already-selected channel scope (for some number repetitions greater than 1, or after the first)
                                                    Log.v("MapView", "\tSame source channel scope " + (scopeIndex + 1) + " touched.");
                                                    // sourceChannelScopeIndex = i; // No need to re-select the scope
                                                    boardSprite.portScopeSprites.get(scopeIndex).channelType = PortScopeSprite.ChannelType.getNextType(boardSprite.portScopeSprites.get(scopeIndex).channelType); // (boardSprite.channelTypes.get(i) + 1) % boardSprite.channelTypeColors.length

                                                    // Narrate
                                                    // ApplicationView.getApplicationView().speakPhrase("setting as input. you can send the data to another board if you want. touch another board.");

                                                    // TODO: Offer options and propose ways to proceed.

                                                    isGestureInProgress = true;

                                                    break;
                                                } else {
                                                    //Touched a different node, so update the source...

                                                    // Touched already-selected channel scope (for some number repetitions greater than 1, or after the first)
                                                    // if (sourceBoardSprite.channelTypes.get(sourceChannelScopeIndex) != BoardSprite.ChannelType.NONE) {
//                                                if (boardSprite.portScopeSprites.get(scopeIndex).channelType != PortScopeSprite.ChannelType.NONE) {
                                                    sourceBoardSprite.portScopeSprites.get(sourceChannelScopeIndex).channelType = PortScopeSprite.ChannelType.NONE; // TODO: Revert to previous type, if there is a previous type. BoardSprite.ChannelType.getNextType(boardSprite.channelTypes.get(i))
//                                                }
                                                    // }

                                                    // Select the just-touched scope as the source.
                                                    sourceChannelScopeIndex = scopeIndex;
                                                    boardSprite.portScopeSprites.get(scopeIndex).channelType = PortScopeSprite.ChannelType.getNextType(boardSprite.portScopeSprites.get(scopeIndex).channelType); // (boardSprite.channelTypes.get(i) + 1) % boardSprite.channelTypeColors.length
                                                    Log.v("MapView", "\tDifferent source channel scope " + (scopeIndex + 1) + " touched.");

                                                    // Narrate
                                                    // ApplicationView.getApplicationView().speakPhrase("setting as input. you can send the data to another board if you want. touch another board.");

                                                    // TODO: Offer options and propose ways to proceed.

                                                    isGestureInProgress = true;

                                                    break;
                                                }
                                            }

                                        } else {

                                        }


                                    }
                                }
                            }
                        }

                    }
                }

//                    } else {
//                        // Repeated touch on the source channel scope
//
//                        if (!isDragging[pointerId]) {
//
//                            for (BoardSprite boardSprite : this.boardSprites) {
//
//                                // Check if the touched board's channel node is touched
//                                for (int scopeIndex = 0; scopeIndex < boardSprite.getChannelCount(); scopeIndex++) {
//                                    // Check if one of the objects is touched
//                                    if (Geometry.getDistance(touchStart[pointerId], boardSprite.portScopeSprites.get(scopeIndex).getPosition()) < 80) {
//
//                                    }
//                                }
//
//                            }
//                        }
//
//                    }
//
//                }
            }

            // Step 3: Touch destination board
            if (!isGestureInProgress) {
                if (sourceBoardSprite != null && sourceChannelScopeIndex != -1
                        && destinationBoardSprite == null && destinationChannelScopeIndex == -1) {
                    Log.v("MapView", "Looking for destination board touch.");

                    // Hide channel scopes (unless dragging)
                    if (!isDragging[pointerId]) {

                        for (SystemSprite systemSprite : this.systemSprites) {
                            for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {

                                // Check if one of the objects is touched
                                if (Geometry.getDistance(touchStart[pointerId], boardSprite.getPosition()) < 80) {
                                    Log.v("MapView", "\tDestination board touched.");
                                    destinationBoardSprite = boardSprite;
                                    boardSprite.showChannelScopes();

                                    ApplicationView.getApplicationView().speakPhrase("that board will be the destination. now choose the output channel.");

                                    isGestureInProgress = true;

                                    break;
                                }

                            }
                        }

                    }

                }
            }

            // Step 4: Touch destination channel scope
            if (!isGestureInProgress) {
                if (sourceBoardSprite != null && sourceChannelScopeIndex != -1
                        && destinationBoardSprite != null && destinationChannelScopeIndex == -1) {
                    Log.v("MapView", "Looking for destination channel scope touch.");

                    // Hide channel scopes (unless dragging)
                    if (!isDragging[pointerId]) {

                        for (SystemSprite systemSprite : this.systemSprites) {
                            for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {

                                // TODO: Add this to an onTouch callback for the sprite's channel nodes
                                // Check if the touched board's I/O node is touched
                                for (int i = 0; i < boardSprite.getChannelCount(); i++) {
                                    // Check if one of the objects is touched
                                    if (Geometry.getDistance(touchStart[pointerId], boardSprite.portScopeSprites.get(i).getPosition()) < 80) {

                                        if (boardSprite == destinationBoardSprite) {
                                            Log.v("MapView", "\tDestination channel scope " + (i + 1) + " touched.");
                                            destinationChannelScopeIndex = i;
                                            boardSprite.portScopeSprites.get(i).channelType = PortScopeSprite.ChannelType.getNextType(boardSprite.portScopeSprites.get(i).channelType); // (boardSprite.channelTypes.get(i) + 1) % boardSprite.channelTypeColors.length


                                            ApplicationView.getApplicationView().speakPhrase("got it. the channel is set up. you can connect components to it now and start using them.");
                                            ApplicationView.getApplicationView().speakPhrase("do you want me to help you connect the components?"); // i.e., start interactive assembly... start by showing component browser. then choose component and get instructions for connecting it. show "okay, done" button.

                                            Log.v("MapViewLink", "Created data path.");

                                            sourceBoardSprite.portScopeSprites.get(sourceChannelScopeIndex).addPath(
                                                    sourceBoardSprite,
                                                    sourceChannelScopeIndex,
                                                    destinationBoardSprite,
                                                    destinationChannelScopeIndex
                                            );

                                            sourceBoardSprite.portScopeSprites.get(sourceChannelScopeIndex).channelDirection = PortScopeSprite.ChannelDirection.INPUT;

                                            destinationBoardSprite.portScopeSprites.get(destinationChannelScopeIndex).channelDirection = PortScopeSprite.ChannelDirection.OUTPUT;

                                            // Reset connection state
                                            sourceBoardSprite = null;
                                            destinationBoardSprite = null;
                                            sourceChannelScopeIndex = -1;
                                            destinationChannelScopeIndex = -1;


                                            isGestureInProgress = true;

                                            break;
                                        }
                                    }
                                }

                            }
                        }
                    }

                }
            }

        } else {

            if (isDragging[pointerId]) {

                // Connection: A complete connection made.
                if (sourceBoardSprite != null && sourceChannelScopeIndex != -1
                        && destinationBoardSprite != null && destinationChannelScopeIndex != -1) {

                    Log.v("MapViewLink", "Created data path.");

                    sourceBoardSprite.portScopeSprites.get(sourceChannelScopeIndex).addPath(
                            sourceBoardSprite,
                            sourceChannelScopeIndex,
                            destinationBoardSprite,
                            destinationChannelScopeIndex
                    );

                    sourceBoardSprite.portScopeSprites.get(sourceChannelScopeIndex).channelDirection = PortScopeSprite.ChannelDirection.INPUT;

                    destinationBoardSprite.portScopeSprites.get(destinationChannelScopeIndex).channelDirection = PortScopeSprite.ChannelDirection.OUTPUT;

                    // Reset connection state
                    sourceBoardSprite = null;
                    destinationBoardSprite = null;
                    sourceChannelScopeIndex = -1;
                    destinationChannelScopeIndex = -1;

                } else if (sourceBoardSprite != null) {

                    Log.v("MapViewLink", "Partial data path was abandoned.");

                    // Reset selected source channel scope
                    if (sourceChannelScopeIndex != -1) {
                        sourceBoardSprite.portScopeSprites.get(sourceChannelScopeIndex).channelType = PortScopeSprite.ChannelType.NONE;
                    }

                    // Reset selected destination channel scope
                    if (destinationChannelScopeIndex != -1) {
                        sourceBoardSprite.portScopeSprites.get(destinationChannelScopeIndex).channelType = PortScopeSprite.ChannelType.NONE;
                    }

                    // Hide scopes.
                    for (SystemSprite systemSprite : this.systemSprites) {
                        for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {
                            boardSprite.hideChannelScopes();
                            boardSprite.hideChannelPaths();
                        }
                    }

                    // Reset connection state
                    sourceBoardSprite = null;
                    destinationBoardSprite = null;
                    sourceChannelScopeIndex = -1;
                    destinationChannelScopeIndex = -1;

                }
            }

        }

        if (!isGestureInProgress) {

            Log.v("MapViewTouch", "Partial data path was abandoned.");

            if (sourceBoardSprite != null && sourceChannelScopeIndex != -1 && destinationBoardSprite != null) {
                ApplicationView.getApplicationView().speakPhrase("the channel was interrupted.");
            }

//            // Reset selected source channel scope
//            if (sourceChannelScopeIndex != -1) {
//                sourceBoardSprite.portScopeSprites.get(sourceChannelScopeIndex).channelType = PortScopeSprite.ChannelType.NONE;
//            }

            // Reset selected destination channel scope
            if (destinationChannelScopeIndex != -1) {
                destinationBoardSprite.portScopeSprites.get(destinationChannelScopeIndex).channelType = PortScopeSprite.ChannelType.NONE;
            }

            // No touch on board or scope. Touch is on map. So hide scopes.
            for (SystemSprite systemSprite : this.systemSprites) {
                for (BoardSprite boardSprite : systemSprite.getBoardSprites()) {
                    boardSprite.hideChannelScopes();
                    boardSprite.hideChannelPaths();
                    boardSprite.setTransparency(1.0f);
                }
            }

            // Reset connection state
            sourceBoardSprite = null;
            destinationBoardSprite = null;
            sourceChannelScopeIndex = -1;
            destinationChannelScopeIndex = -1;

            // Reset map interactivity
            isPanningDisabled = false;
        }

        // Stop touching sprite
        // Style. Reset the style of touched boards.
        if (isTouching[pointerId] || touchedSprite[pointerId] != null) {
            isTouching[pointerId] = false;
            if (touchedSprite[pointerId] != null) {
                touchedSprite[pointerId].showHighlights = false;
                touchedSprite[pointerId].setScale(1.0f);
                touchedSprite[pointerId] = null;
            }
        }

        // Stop dragging
        this.isDragging[pointerId] = false;

    }
}
