package camp.computer.clay.sequencer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class CustomListView extends ListView {

    private static final boolean HIDE_LIST_ITEM_SEPARATOR = true;
    private static final boolean HIDE_ABSTRACT_OPTION = true;

    private CustomAdapter adapter;
    private ArrayList<ListItem> data; // The data to display in _this_ ListView. This has to be repopulated on initialization.

    boolean itemHasFocus = false;
    ListItem itemWithFocus = null;
    private ListItem movingItem = null;
//    private int movingItemFrom = -1;

    public CustomListView(Context context) {
        super(context);
        init ();
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize the ListView.
     */
    public void init()
    {
        initLayout();

        initData();

        // Set up data adapter
//        adapter = new ArrayAdapter<String>(getContext(),R.layout.list_item_type_light, R.id.label, data);
        // setup the data adaptor
        this.adapter = new CustomAdapter(getContext(), R.layout.list_item_type_light, this.data);
        setAdapter(adapter);

        // Set up gesture recognition
        setOnTouchListener(new ListTouchListener());
        setOnItemClickListener(new ListSelection());
        setOnItemLongClickListener(new ListLongSelection());
        setOnDragListener(new ListDrag());
    }

    private void initLayout() {
        if (CustomListView.HIDE_LIST_ITEM_SEPARATOR) {
            setDivider(null);
            setDividerHeight(0);
        }
    }

    /**
     * Set up the data source and populate the list of data to show in this ListView.
     */
    public void initData () {
        // TODO: Initialize data from cache or from remote source in this function. Do this because the ViewPager will destroy this object when moving between pages.

        // setup the data source
        this.data = new ArrayList<ListItem>();

        // create some objects... and add them into the array list
        if (!CustomListView.HIDE_ABSTRACT_OPTION) {
            this.data.add(new ListItem("abstract", "Subtitle", CustomAdapter.SYSTEM_CONTROL_LAYOUT));
        }

        // Basic behaviors
        this.data.add(new ListItem("lights", "Subtitle", CustomAdapter.LIGHT_CONTROL_LAYOUT));
        this.data.add(new ListItem("io", "Subtitle", CustomAdapter.IO_CONTROL_LAYOUT));
        this.data.add(new ListItem("message", "turn lights on", CustomAdapter.MESSAGE_CONTROL_LAYOUT));
        this.data.add(new ListItem("wait", "500 ms", CustomAdapter.WAIT_CONTROL_LAYOUT));
        this.data.add(new ListItem("say", "oh, that's great", CustomAdapter.SAY_CONTROL_LAYOUT));

        this.data.add(new ListItem("create", "Subtitle", CustomAdapter.SYSTEM_CONTROL_LAYOUT));
    }

    /**
     * Add data to the ListView.
     *
     * @param item
     */
    private void addData (ListItem item) {
        if (adapter != null) {
            data.add(data.size() - 1, item);
            updateViewFromData();
        }
    }

    public void updateViewFromData () {
        // TODO: Perform callbacks into data model to propagate changes based on view state and data item state.
        adapter.notifyDataSetChanged();
    }

    private void displayListItemOptions(final ListItem item) {
        int basicBehaviorCount = 7;
        final String[] behaviorOptions = new String[basicBehaviorCount];
        // loop, condition, branch
        behaviorOptions[0] = "delete";
        behaviorOptions[1] = "configure";
        behaviorOptions[2] = "change type";
        behaviorOptions[3] = (item.selected ? "deselect" : "select");
        behaviorOptions[4] = (item.repeat ? "do once" : "repeat");
        behaviorOptions[5] = "add condition";
        behaviorOptions[6] = "move";
        // cause/effect (i.e., condition)
        // HTTP API interface (general wrapper, with authentication options)

        // Show the list of behaviors
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Behavior options");
        builder.setItems(behaviorOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemIndex) {

                if (behaviorOptions[itemIndex].toString().equals("delete")) {

                    deleteListItem (item);

                } else if (behaviorOptions[itemIndex].toString().equals("configure")) {

                    displayConfigureController (item);

                } else if (behaviorOptions[itemIndex].toString().equals("change type")) {

                    selectBehaviorType(item);

                } else if (behaviorOptions[itemIndex].toString().equals("select") || behaviorOptions[itemIndex].toString().equals("deselect")) {

                    if (behaviorOptions[itemIndex].toString().equals("select")) {
                        selectListItem(item);
                    } else if (behaviorOptions[itemIndex].toString().equals("deselect")) {
                        deselectListItem(item);
                    }

                } else if (behaviorOptions[itemIndex].toString().equals("repeat") || behaviorOptions[itemIndex].toString().equals("do once")) {

                    if (behaviorOptions[itemIndex].toString().equals("repeat")) {
                        repeatListItem(item);
                    } else if (behaviorOptions[itemIndex].toString().equals("do once")) {
                        stepListItem(item);
                    }

                } else if (behaviorOptions[itemIndex].toString().equals("move")) {

                    startMoveListItem (item);

                }

                updateViewFromData();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void startMoveListItem(ListItem item) {
        movingItem = item;
        item.moving = true;
    }

    private void moveListItem (int toIndex) {

        // Remove the item from the old position...
        data.remove(movingItem);
        updateViewFromData();

        // ...and move the data to the new position...
        data.add(toIndex, movingItem);
        updateViewFromData();

        // ...then reset the state that denotes that a move is being performed.
        movingItem.moving = false;
        movingItem = null;
    }

    public void displayConfigureController (final ListItem item) {

        if (item.type == CustomAdapter.LIGHT_CONTROL_LAYOUT) {
            displayConfigureLights (item);
        } else if (item.type == CustomAdapter.IO_CONTROL_LAYOUT) {
            displayConfigureIO (item);
        } else if (item.type == CustomAdapter.MESSAGE_CONTROL_LAYOUT) {
            displayConfigureMessage(item);
        } else if (item.type == CustomAdapter.WAIT_CONTROL_LAYOUT) {
            displayConfigureWait(item);
        } else if (item.type == CustomAdapter.SAY_CONTROL_LAYOUT) {
            displayConfigureSay(item);
        }

    }

    public void displayConfigureLights (final ListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle ("Change the channel.");
        builder.setMessage ("What do you want to do?");

        // Declare transformation layout
        LinearLayout transformLayout = new LinearLayout (getContext());
        transformLayout.setOrientation (LinearLayout.VERTICAL);

        // Set up the LED label
        final TextView lightLabel = new TextView (getContext());
        lightLabel.setText("Enable LED feedback");
        lightLabel.setPadding(70, 20, 70, 20);
        transformLayout.addView(lightLabel);

        LinearLayout lightLayout = new LinearLayout (getContext());
        lightLayout.setOrientation(LinearLayout.HORIZONTAL);
//        channelLayout.setLayoutParams (new LinearLayout.LayoutParams (MATCH_PARENT));
        final ArrayList<ToggleButton> lightToggleButtons = new ArrayList<> ();
        for (int i = 0; i < 12; i++) {
            final String channelLabel = Integer.toString(i + 1);
            final ToggleButton toggleButton = new ToggleButton (getContext());
            toggleButton.setPadding(0, 0, 0, 0);
            toggleButton.setText(channelLabel);
            toggleButton.setTextOn(channelLabel);
            toggleButton.setTextOff(channelLabel);
            // e.g., LinearLayout.LayoutParams param = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            params.setMargins (0, 0, 0, 0);
            toggleButton.setLayoutParams (params);
            lightToggleButtons.add (toggleButton); // Add the button to the list.
            lightLayout.addView(toggleButton);
        }

        transformLayout.addView (lightLayout);

        // Assign the layout to the alert dialog.
        builder.setView (transformLayout);

        // Set up the buttons
        builder.setPositiveButton ("DONE", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {
//                Hack_behaviorTitle = input.getText ().toString ();
                String transformString = "apply ";
                // Add the LED state
//                for (int i = 0; i < 12; i++) {
//                    if (lightToggleButtons.get(i).isChecked()) {
//                        transformString = transformString.concat(" 1");
//                    } else {
//                        transformString = transformString.concat(" 0");
//                    }
//                }

                for (int i = 0; i < 12; i++) {

                    final ToggleButton lightEnableButton = lightToggleButtons.get (i);

                    // LED enable. Is the LED on or off?

                    if (lightEnableButton.isChecked ()) {
                        transformString = transformString.concat ("T");
                        item.lightStates.set(i, true);
                    } else {
                        transformString = transformString.concat ("F");
                        item.lightStates.set(i, false);
                    }
                    // transformString = transformString.concat (","); // Add comma

                    // TODO: Set LED color.

                    // Add space between channel states.
                    if (i < (12 - 1)) {
                        transformString = transformString.concat (" ");
                    }
                }

                // Add wait
//                Hack_BehaviorTransformTitle = transformString;
//                Behavior behavior = new Behavior ("transform");
//                behavior.setTransform(Hack_BehaviorTransformTitle);
//                BehaviorConstruct behaviorConstruct = new BehaviorConstruct (perspective);
//                behaviorConstruct.setBehavior(behavior);
//                perspective.addBehaviorConstruct(behaviorConstruct);

                // TODO: Store the state of the lights in the object associated with the ListItem

                // Refresh the timeline view
                updateViewFromData();
            }
        });
        builder.setNegativeButton ("Cancel", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                dialog.cancel ();
            }
        });

        builder.show ();
    }

    public void displayConfigureIO (final ListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle ("Change the channel.");
        builder.setMessage ("What do you want to do?");

        // TODO: Populate with the current transform values (if any).
//        if (behaviorConstruct.getBehavior().getTransform() != null) {
//            Log.v("Behavior_Transform", behaviorConstruct.getBehavior().getTransform());
//            // TODO: Store the previous values so they can be used to initialize the interface.
//        }

        // TODO: Specify the units to receive the change.

        // Declare transformation layout
        LinearLayout transformLayout = new LinearLayout (getContext());
        transformLayout.setOrientation(LinearLayout.VERTICAL);

        // Channels

        final ArrayList<ToggleButton> channelEnableToggleButtons = new ArrayList<> ();
        final ArrayList<Button> channelDirectionButtons = new ArrayList<> ();
        final ArrayList<Button> channelModeButtons = new ArrayList<> ();
        final ArrayList<ToggleButton> channelValueToggleButtons = new ArrayList<> ();

        // Set up the channel label
        final TextView channelEnabledLabel = new TextView (getContext());
        channelEnabledLabel.setText("Enable channels");
        channelEnabledLabel.setPadding(70, 20, 70, 20);
        transformLayout.addView (channelEnabledLabel);

        LinearLayout channelEnabledLayout = new LinearLayout (getContext());
        channelEnabledLayout.setOrientation (LinearLayout.HORIZONTAL);
        for (int i = 0; i < 12; i++) {
            final String channelLabel = Integer.toString (i + 1);
            final ToggleButton toggleButton = new ToggleButton (getContext());
            toggleButton.setPadding(0, 0, 0, 0);
            toggleButton.setText (channelLabel);
            toggleButton.setTextOn (channelLabel);
            toggleButton.setTextOff (channelLabel);
            // e.g., LinearLayout.LayoutParams param = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            params.setMargins (0, 0, 0, 0);
            toggleButton.setLayoutParams (params);
            channelEnableToggleButtons.add (toggleButton); // Add the button to the list.
            channelEnabledLayout.addView (toggleButton);
        }
        transformLayout.addView (channelEnabledLayout);

        // Set up the label
        final TextView signalLabel = new TextView (getContext());
        signalLabel.setText ("Set channel direction, mode, and value"); // INPUT: Discrete/Digital, Continuous/Analog; OUTPUT: Discrete, Continuous/PWM
        signalLabel.setPadding (70, 20, 70, 20);
        transformLayout.addView (signalLabel);

        // Show I/O options
        final LinearLayout ioLayout = new LinearLayout (getContext());
        ioLayout.setOrientation (LinearLayout.HORIZONTAL);
        for (int i = 0; i < 12; i++) {
            final String channelLabel = Integer.toString (i + 1);
            final Button toggleButton = new Button (getContext());
            toggleButton.setPadding (0, 0, 0, 0);
            toggleButton.setText(" ");
            toggleButton.setEnabled (false); // TODO: Initialize with current state at the behavior's location in the loop! That is allow defining _changes to_ an existing state, so always work from grounded state material.
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            toggleButton.setLayoutParams (params);
            channelDirectionButtons.add (toggleButton); // Add the button to the list.
            ioLayout.addView (toggleButton);
        }
        transformLayout.addView (ioLayout);

        /*
        // Set up the I/O mode label
        final TextView ioModeLabel = new TextView (this);
        ioModeLabel.setText ("I/O Mode"); // INPUT: Discrete/Digital, Continuous/Analog; OUTPUT: Discrete, Continuous/PWM
        ioModeLabel.setPadding (70, 20, 70, 20);
        transformLayout.addView (ioModeLabel);
        */

        // Show I/O selection mode (Discrete or Continuous)
        LinearLayout channelModeLayout = new LinearLayout (getContext());
        channelModeLayout.setOrientation (LinearLayout.HORIZONTAL);
        for (int i = 0; i < 12; i++) {
            final Button toggleButton = new Button (getContext());
            toggleButton.setPadding (0, 0, 0, 0);
            toggleButton.setText(" ");
            toggleButton.setEnabled (false); // TODO: Initialize with current state at the behavior's location in the loop! That is allow defining _changes to_ an existing state, so always work from grounded state material.
            // e.g., LinearLayout.LayoutParams param = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            toggleButton.setLayoutParams (params);
            channelModeButtons.add (toggleButton); // Add the button to the list.
            channelModeLayout.addView (toggleButton);
        }
        transformLayout.addView (channelModeLayout);

        // Value. Show channel value.
        LinearLayout channelValueLayout = new LinearLayout (getContext());
        channelValueLayout.setOrientation (LinearLayout.HORIZONTAL);
//        channelLayout.setLayoutParams (new LinearLayout.LayoutParams (MATCH_PARENT));
        for (int i = 0; i < 12; i++) {
            // final String buttonLabel = Integer.toString (i + 1);
            final ToggleButton toggleButton = new ToggleButton (getContext());
            toggleButton.setPadding(0, 0, 0, 0);
            toggleButton.setEnabled (false);
            toggleButton.setText (" ");
            toggleButton.setTextOn ("H");
            toggleButton.setTextOff ("L");
            // e.g., LinearLayout.LayoutParams param = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            params.setMargins (0, 0, 0, 0);
            toggleButton.setLayoutParams (params);
            channelValueToggleButtons.add (toggleButton); // Add the button to the list.
            channelValueLayout.addView (toggleButton);
        }
        transformLayout.addView (channelValueLayout);

        // Set up interactivity for channel enable buttons.
        for (int i = 0; i < 12; i++) {

            final ToggleButton channelEnableButton = channelEnableToggleButtons.get (i);
            final Button channelDirectionButton = channelDirectionButtons.get (i);
            final Button channelModeButton = channelModeButtons.get (i);
            final ToggleButton channelValueToggleButton = channelValueToggleButtons.get (i);

            channelEnableButton.setOnCheckedChangeListener (new CompoundButton.OnCheckedChangeListener () {
                @Override
                public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {

                    if (channelDirectionButton.getText ().toString ().equals (" ")) {
                        channelDirectionButton.setText ("I");
                    }
                    channelDirectionButton.setEnabled (isChecked);

                    if (channelModeButton.getText ().toString ().equals (" ")) {
                        channelModeButton.setText ("T");
                    }
                    channelModeButton.setEnabled (isChecked);

                    if (isChecked == false) {
                        channelValueToggleButton.setEnabled (isChecked);
                    }
                }
            });
        }

        // Setup interactivity for I/O options
        for (int i = 0; i < 12; i++) {

            final Button channelDirectionButton = channelDirectionButtons.get (i);
            final Button channelModeButton = channelModeButtons.get (i);
            final ToggleButton channelValueToggleButton = channelValueToggleButtons.get (i);

            channelDirectionButton.setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick (View v) {
                    String currentText = channelDirectionButton.getText ().toString ();
                    if (currentText.equals (" ")) {
                        channelDirectionButton.setText ("I");
                        channelModeButton.setEnabled (true);

                        // Update modes for input channel.
                        if (channelModeButton.getText ().toString ().equals (" ")) {
                            channelModeButton.setText ("T");
                        }
                        channelModeButton.setOnClickListener (new View.OnClickListener () {
                            @Override
                            public void onClick (View v) {
                                String currentText = channelModeButton.getText ().toString ();
                                if (currentText.equals (" ")) { // Do not change. Keep current state.

                                    channelModeButton.setText ("T"); // Toggle.

                                    // Update values for output channel.
                                    channelValueToggleButton.setEnabled (true);
                                    if (channelValueToggleButton.getText ().toString ().equals (" ")) {
                                        channelValueToggleButton.setText ("L");
                                    }

                                } else if (currentText.equals ("T")) {
                                    channelModeButton.setText ("W"); // Waveform.
                                    channelValueToggleButton.setEnabled (false); // Update values for output channel.
                                } else if (currentText.equals ("W")) {
                                    channelModeButton.setText ("P"); // Pulse.
                                    channelValueToggleButton.setEnabled (false); // Update values for output channel.
                                } else if (currentText.equals ("P")) {
                                    channelModeButton.setText ("T"); // Toggle

                                    // Update values for output channel.
                                    channelValueToggleButton.setEnabled (true);
                                    if (channelValueToggleButton.getText ().toString ().equals (" ")) {
                                        channelValueToggleButton.setText ("L");
                                    }
                                }
                            }
                        });

                    } else if (currentText.equals ("I")) {
                        channelDirectionButton.setText ("O");

                        // Update modes for output channel.
                        channelModeButton.setEnabled (true);
                        if (channelModeButton.getText ().toString ().equals (" ")) {
                            channelModeButton.setText ("T");
                        }
                        channelModeButton.setOnClickListener (new View.OnClickListener () {
                            @Override
                            public void onClick (View v) {
                                String currentText = channelModeButton.getText ().toString ();
                                if (currentText.equals (" ")) { // Do not change. Keep current state.

                                    channelModeButton.setText ("T"); // Toggle.

                                    // Update values for output channel.
                                    channelValueToggleButton.setEnabled (true);
                                    if (channelValueToggleButton.getText ().toString ().equals (" ")) {
                                        channelValueToggleButton.setText ("L");
                                    }

                                } else if (currentText.equals ("T")) {
                                    channelModeButton.setText ("W"); // Waveform.
                                    channelValueToggleButton.setEnabled (false); // Update values for output channel.
                                } else if (currentText.equals ("W")) {
                                    channelModeButton.setText ("P"); // Pulse.
                                    channelValueToggleButton.setEnabled (false); // Update values for output channel.
                                } else if (currentText.equals ("P")) {
                                    channelModeButton.setText ("T"); // Toggle

                                    // Update values for output channel.
                                    channelValueToggleButton.setEnabled (true);
                                    if (channelValueToggleButton.getText ().toString ().equals (" ")) {
                                        channelValueToggleButton.setText ("L");
                                    }
                                }
                            }
                        });

                    } else if (currentText.equals ("O")) {
                        channelDirectionButton.setText ("I");
                        channelModeButton.setEnabled (true);

                        // Update modes for input channel.
                        if (channelModeButton.getText ().toString ().equals (" ")) {
                            channelModeButton.setText ("T");
                        }
                        channelModeButton.setOnClickListener (new View.OnClickListener () {
                            @Override
                            public void onClick (View v) {
                                String currentText = channelModeButton.getText ().toString ();
                                if (currentText.equals (" ")) { // Do not change. Keep current state.
                                    channelModeButton.setText ("T"); // Toggle.

                                    // Update values for output channel.
                                    channelValueToggleButton.setEnabled (true);
                                    if (channelValueToggleButton.getText ().toString ().equals (" ")) {
                                        channelValueToggleButton.setText ("L");
                                    }

                                } else if (currentText.equals ("T")) {
                                    channelModeButton.setText ("W"); // Waveform.
                                    channelValueToggleButton.setEnabled (false); // Update values for output channel.
                                } else if (currentText.equals ("W")) {
                                    channelModeButton.setText ("P"); // Pulse.
                                    channelValueToggleButton.setEnabled (false); // Update values for output channel.
                                } else if (currentText.equals ("P")) {
                                    channelModeButton.setText ("T"); // Toggle

                                    // Update values for output channel.
                                    channelValueToggleButton.setEnabled (true);
                                    if (channelValueToggleButton.getText ().toString ().equals (" ")) {
                                        channelValueToggleButton.setText ("L");
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }

        // Assign the layout to the alert dialog.
        builder.setView (transformLayout);

        // Set up the buttons
        builder.setPositiveButton ("DONE", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {
//                Hack_behaviorTitle = input.getText ().toString ();
                String transformString = "apply ";
                // Add the LED state
//                for (int i = 0; i < 12; i++) {
//                    if (lightToggleButtons.get(i).isChecked()) {
//                        transformString = transformString.concat(" 1");
//                    } else {
//                        transformString = transformString.concat(" 0");
//                    }
//                }

                for (int i = 0; i < 12; i++) {

                    final ToggleButton channelEnableButton = channelEnableToggleButtons.get (i);
                    final Button channelDirectionButton = channelDirectionButtons.get (i);
                    final Button channelModeButton = channelModeButtons.get (i);
                    final ToggleButton channelValueToggleButton = channelValueToggleButtons.get (i);

                    // Channel enable. Is the channel enabled?

                    if (channelEnableButton.isChecked ()) {
                        transformString = transformString.concat ("T");
                        item.ioStates.set(i, true);
                    } else {
                        transformString = transformString.concat ("F");
                        item.ioStates.set(i, false);
                    }
                    // transformString = transformString.concat (","); // Add comma

                    // Channel I/O direction. Is the I/O input or output?

                    if (channelDirectionButton.isEnabled ()) {
                        String channelDirectionString = channelDirectionButton.getText ().toString ();
                        transformString = transformString.concat (channelDirectionString);
                    } else {
                        transformString = transformString.concat ("-");
                    }
                    // transformString = transformString.concat (","); // Add comma

                    // Channel I/O mode. Is the channel toggle switch (discrete), waveform (continuous), or pulse?

                    if (channelModeButton.isEnabled ()) {
                        String channelModeString = channelModeButton.getText ().toString ();
                        transformString = transformString.concat (channelModeString);
                    } else {
                        transformString = transformString.concat ("-");
                    }

                    // Channel value.
                    // TODO: Create behavior transform to apply channel values separately. This transform should only configure the channel operational flow state.

                    if (channelValueToggleButton.isEnabled ()) {
                        String channelValueString = channelValueToggleButton.getText ().toString ();
                        transformString = transformString.concat (channelValueString);
                    } else {
                        transformString = transformString.concat ("-");
                    }

                    // Add space between channel states.
                    if (i < (12 - 1)) {
                        transformString = transformString.concat (" ");
                    }
                }

                // Add wait
//                Hack_BehaviorTransformTitle = transformString;
//                Behavior behavior = new Behavior ("transform");
//                behavior.setTransform(Hack_BehaviorTransformTitle);
//                BehaviorConstruct behaviorConstruct = new BehaviorConstruct (perspective);
//                behaviorConstruct.setBehavior(behavior);
//                perspective.addBehaviorConstruct(behaviorConstruct);

                // TODO: Store the state of the lights in the object associated with the ListItem

                // Refresh the timeline view
                updateViewFromData();
            }
        });
        builder.setNegativeButton ("Cancel", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                dialog.cancel ();
            }
        });

        builder.show ();
    }

    public void displayConfigureMessage (final ListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle ("what's the message?");

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);//input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Hack_PromptForSpeechTitle = input.getText().toString();
//                Behavior behavior = new Behavior("say");
//                behavior.setTransform(Hack_PromptForSpeechTitle);
//                BehaviorConstruct behaviorConstruct = new BehaviorConstruct(perspective);
//                behaviorConstruct.setBehavior(behavior);
//                perspective.addBehaviorConstruct(behaviorConstruct);

                item.message = input.getText().toString();

                // Refresh the timeline view
                updateViewFromData();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show ();
    }

    public void displayConfigureSay (final ListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle ("tell me the behavior");

        // Set up the input
        final EditText input = new EditText(getContext());
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);//input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("DONE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Hack_PromptForSpeechTitle = input.getText().toString();
//                Behavior behavior = new Behavior("say");
//                behavior.setTransform(Hack_PromptForSpeechTitle);
//                BehaviorConstruct behaviorConstruct = new BehaviorConstruct(perspective);
//                behaviorConstruct.setBehavior(behavior);
//                perspective.addBehaviorConstruct(behaviorConstruct);

                item.phrase = input.getText().toString();

                // Refresh the timeline view
                updateViewFromData();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show ();
    }

    public void displayConfigureWait (final ListItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle ("Time Transform");
        builder.setMessage ("How do you want to change time?");

        // Declare transformation layout
        LinearLayout transformLayout = new LinearLayout (getContext());
        transformLayout.setOrientation (LinearLayout.VERTICAL);

        // Wait (until next behavior)

        // Set up the label
        final TextView waitLabel = new TextView (getContext());
        waitLabel.setText ("Wait (0 ms)");
        waitLabel.setPadding (70, 20, 70, 20);
        transformLayout.addView (waitLabel);

        final SeekBar waitVal = new SeekBar (getContext());
        waitVal.setMax (1000);
        waitVal.setHapticFeedbackEnabled (true); // TODO: Emulate this in the custom interface
        waitVal.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener () {
            @Override
            public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
                waitLabel.setText ("Wait (" + progress + " ms)");
            }

            @Override
            public void onStartTrackingTouch (SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch (SeekBar seekBar) {

            }
        });
        transformLayout.addView (waitVal);

        // Assign the layout to the alert dialog.
        builder.setView (transformLayout);

        // Set up the buttons
        builder.setPositiveButton ("DONE", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {

                // Create transform string
                String transformString = "";

                // Add wait
//                transformString = transformString.concat (Integer.toString (waitVal.getProgress ()));
//                Hack_TimeTransformTitle = transformString;
//                Behavior behavior = new Behavior ("time");
//                behavior.setTransform(Hack_TimeTransformTitle);
//                BehaviorConstruct behaviorConstruct = new BehaviorConstruct (perspective);
//                behaviorConstruct.setBehavior(behavior);
//                perspective.addBehaviorConstruct(behaviorConstruct);
                item.time = waitVal.getProgress ();

                // Refresh the timeline view
                updateViewFromData();
            }
        });
        builder.setNegativeButton ("Cancel", new DialogInterface.OnClickListener () {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                dialog.cancel ();
            }
        });

        builder.show ();
    }

    private void selectListItem (final ListItem item) {

        // Do not select system controllers
        if (item.type == CustomAdapter.SYSTEM_CONTROL_LAYOUT || item.type == CustomAdapter.CONTROL_PLACEHOLDER_LAYOUT) {
            item.selected = false;
            return;
        }

        // Update state of the object associated with the selected view.
        if (item.selected == false) {
            item.selected = true;
        }

    }

    private void deselectListItem (final ListItem item) {

        // Update state of the object associated with the selected view.
        if (item.selected == true) {
            item.selected = false;
        }

    }

    private void stepListItem(ListItem item) {

        if (item.repeat == true) {
            item.repeat = false;
        }
    }

    private void repeatListItem(ListItem item) {

        if (item.repeat == false) {
            item.repeat = true;
        }
    }

    private void deleteListItem (final ListItem item) {

        // Update state of the object associated with the selected view.
        data.remove(item);

        // Update the view after removing the specified list item
        updateViewFromData();

    }

    private void selectBehaviorType (final ListItem item) {
        int basicBehaviorCount = 5;
        final String[] basicBehaviors = new String[basicBehaviorCount];
        // loop, condition, branch
        basicBehaviors[0] = "lights";
        basicBehaviors[1] = "io";
        basicBehaviors[2] = "message"; // send, look for, wait for
        basicBehaviors[3] = "wait"; // time
        basicBehaviors[4] = "say";
        // cause/effect (i.e., condition)
        // HTTP API interface (general wrapper, with authentication options)

        // Show the list of behaviors
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select a behavior");
        builder.setItems(basicBehaviors, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemIndex) {

                if (basicBehaviors[itemIndex].toString().equals("lights")) {
//                            Hack_PromptForBehaviorTransform(perspective);

                    // <HACK>
                    // This removes the specified item from the list and replaces it with an item of a specific type.
                    // TODO: Replace view, not data! (i.e., item.type = CustomAdapter.LIGHT_CONTROL_LAYOUT;)
                    int index = data.indexOf(item);
                    data.remove(index);
                    updateViewFromData();
                    // Add the new item.
                    ListItem replacementItem = new ListItem("lights", "", CustomAdapter.LIGHT_CONTROL_LAYOUT);
                    data.add(index, replacementItem);
                    // </HACK>

                } else if (basicBehaviors[itemIndex].toString().equals("io")) {

//                            Hack_PromptForBehaviorTransform(perspective);

                    // <HACK>
                    // This removes the specified item from the list and replaces it with an item of a specific type.
                    // TODO: Replace view, not data! (i.e., item.type = CustomAdapter.CONTROL_PLACEHOLDER_LAYOUT;)
                    int index = data.indexOf(item);
                    data.remove(index);
                    updateViewFromData();
                    // Add the new item.
                    ListItem replacementItem = new ListItem("io", "", CustomAdapter.IO_CONTROL_LAYOUT);
                    data.add(index, replacementItem);
                    // </HACK>

                } else if (basicBehaviors[itemIndex].toString().equals("wait")) {

//                            Hack_PromptForTimeTransform(perspective);

                    // <HACK>
                    // This removes the specified item from the list and replaces it with an item of a specific type.
                    // TODO: Replace view, not data! (i.e., item.type = CustomAdapter.WAIT_CONTROL_LAYOUT;)
                    int index = data.indexOf(item);
                    data.remove(index);
                    updateViewFromData();
                    // Add the new item.
                    ListItem replacementItem = new ListItem("wait", "500 ms", CustomAdapter.WAIT_CONTROL_LAYOUT);
                    data.add(index, replacementItem);
                    // </HACK>

                } else if (basicBehaviors[itemIndex].toString().equals("message")) {

//                            Hack_PromptForMessage(perspective);

                    // <HACK>
                    // This removes the specified item from the list and replaces it with an item of a specific type.
                    // TODO: Replace view, not data! (i.e., item.type = CustomAdapter.MESSAGE_CONTROL_LAYOUT;)
                    int index = data.indexOf(item);
                    data.remove(index);
                    updateViewFromData();
                    // Add the new item.
                    ListItem replacementItem = new ListItem("message", "turn lights off", CustomAdapter.MESSAGE_CONTROL_LAYOUT);
                    data.add(index, replacementItem);
                    // </HACK>

                } else if (basicBehaviors[itemIndex].toString().equals("say")) {

//                            Hack_PromptForSpeech(perspective);

                    // <HACK>
                    // This removes the specified item from the list and replaces it with an item of a specific type.
                    // TODO: Replace view, not data! (i.e., item.type = CustomAdapter.MESSAGE_CONTROL_LAYOUT;)
                    int index = data.indexOf(item);
                    data.remove(index);
                    updateViewFromData();
                    // Add the new item.
                    ListItem replacementItem = new ListItem("say", "what do you think?", CustomAdapter.SAY_CONTROL_LAYOUT);
                    data.add(index, replacementItem);
                    // </HACK>

                }

                updateViewFromData();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class ListDrag implements OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            Log.v ("Gesture_Log", "OnDragLister from CustomListView");
            return false;
        }
    }

    private class ListTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.v ("Gesture_Log", "OnTouchListener from CustomListView");

            if (event.getAction() == MotionEvent.ACTION_UP) {

                // Move the item being moved, if any
                if (movingItem != null) {

//                    ListItem item = getListItemAtPosition ((int) event.getRawX(), (int) event.getRawY());
//                    int newPosition = getPositionForView(v);
                    int newPosition = getViewIndexByPosition ((int) event.getRawX(), (int) event.getRawY());
                    moveListItem (newPosition);

//                    movingItem = null;
                }

                if (itemHasFocus) {
                    Log.v ("Gesture_Log_2", "itemHasFocus (CustomListView.onTouch) = " + itemHasFocus);

                    // TODO: Set to itemHasFocus to false if the touch was not in its bounding rect!

                    // Get the list item corresponding to the specified touch point
                    ListItem item = getListItemAtPosition ((int) event.getRawX(), (int) event.getRawY());

                    // Remove focus from the item with focus (if any) if it was not touched
                    if (item == null || itemWithFocus != item) {
                        Log.v ("Gesture_Log_2", "Removing focus");

                        // Remove focus from the item that has it.
                        if (itemWithFocus != null) {
                            itemWithFocus.hasFocus = false;
                            updateViewFromData();
                        }

                        // If touching the item that has focus, remove it from focus.
                        itemHasFocus = false;
                        itemWithFocus = null;
                    }
                    Log.v ("Gesture_Log_2", "itemHasFocus (after) = " + itemHasFocus);
                }

            }

            return false;
        }
    }

    /**
     * Returns the list item corresponding to the specified position.
     * @param x
     * @param y
     * @return
     */
    public ListItem getListItemAtPosition(int x, int y) {
        // Get the list item corresponding to the specified touch point
        int position = getViewIndexByPosition(x, y);
        ListItem item = (ListItem) getItemAtPosition(position);
        return item;
    }

    private class ListLongSelection implements OnItemLongClickListener
    {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            final ListItem item = (ListItem) data.get (position);

            // Check if the list item was a constructor
            if (item.type == CustomAdapter.SYSTEM_CONTROL_LAYOUT) {
                if (item.title == "create") {
                    String title = "";
                    String subtitle = "";
                    int type = CustomAdapter.CONTROL_PLACEHOLDER_LAYOUT;

                    addData (new ListItem (title, subtitle, type));
                }
                // TODO: (?)

            } else if (item.type != CustomAdapter.SYSTEM_CONTROL_LAYOUT && item.type != CustomAdapter.CONTROL_PLACEHOLDER_LAYOUT) {

                if (item.type == CustomAdapter.COMPLEX_LAYOUT) {

                    unabstractSelectedItem (item);

                } else {

                    displayListItemOptions (item);

                }

                /*
                // Show options
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("You pressed item #" + (position + 1));
                builder.setPositiveButton("OK", null);
                builder.show();
                */

                // Request the ListView to be redrawn so the views in it will be displayed
                // according to their updated state information.
                updateViewFromData();
            }

            return false;
        }
    }

    private class ListSelection implements OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, final View view, final int position, long id)
        {
            Log.v("Gesture_Log", "OnItemClickListener from CustomListView");

            final ListItem item = (ListItem) data.get (position);

            // Check if the list item was a constructor
            if (item.type == CustomAdapter.SYSTEM_CONTROL_LAYOUT) {
                if (item.title == "create") {
                    String title = "";
                    String subtitle = "";
                    int type = CustomAdapter.CONTROL_PLACEHOLDER_LAYOUT;

                    addData (new ListItem (title, subtitle, type));
                } else if (item.title == "abstract") {

                    abstractSelectedItems ();

                }
                // TODO: (?)
            } else if (item.type == CustomAdapter.CONTROL_PLACEHOLDER_LAYOUT) {

                // Show options
//                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                builder.setTitle("Select a behavior");
//                builder.setMessage("You pressed item #" + (position + 1));
//                builder.setPositiveButton("OK", null);
//                builder.show();

                selectBehaviorType (item);

            } else {

                Log.v ("Gesture_Log_2", "itemWithFocus = " + itemWithFocus);
                Log.v ("Gesture_Log_2", "         item = " + item);

                Log.v ("Gesture_Log_2", "itemHasFocus (before) = " + itemHasFocus);
                if (itemWithFocus == null) {
                    // If there is no item with focus, focus on the touched item.
                    itemHasFocus = true;
                    itemWithFocus = item;
                    item.hasFocus = true;
                    updateViewFromData();
                }

                else if (itemWithFocus != item) {
                    // If touching the item that has focus, remove it from focus.
                    itemHasFocus = false;
                    itemWithFocus = null;
                    item.hasFocus = false;
                    updateViewFromData();
                }
                Log.v ("Gesture_Log_2", "itemHasFocus (after) = " + itemHasFocus);

            }

        }

    }

    public void abstractSelectedItems() {

        int index = 0;

        // Get list of the selected items
        ArrayList<ListItem> selectedListItems = new ArrayList<>();
        for (ListItem listItem : this.data) {
            if (listItem.selected) {
                listItem.selected = false; // Deselect the item about to be abstracted
                selectedListItems.add(listItem);
            }
            if (selectedListItems.size() == 0) {
                index++;
            }
        }

        // Return if there are fewer than two selected items
        if (selectedListItems.size() < 2) {
            return;
        }

        // Get the first item in the sequence
        ListItem item = selectedListItems.get(0);

        // Remove the selected items from the list
        for (ListItem listItem : selectedListItems) {
            data.remove(listItem);
        }
        updateViewFromData(); // Update view after removing items from the list

        // Create a new abstract item in the list that represents the selected item sequence at the position of the first item in the sequence

        // <HACK>
        // This removes the specified item from the list and replaces it with an item of a specific type.
        // TODO: Replace view, not data! (i.e., item.type = CustomAdapter.LIGHT_CONTROL_LAYOUT;)
//        int index = data.indexOf(item);
//        data.remove(index);
        // Add the new item.
        ListItem replacementItem = new ListItem("complex", "", CustomAdapter.COMPLEX_LAYOUT);
        replacementItem.listItems.addAll(selectedListItems); // Add the selected items to the list
        replacementItem.summary = "" + selectedListItems.size() + " behaviors";
        data.add(index, replacementItem);
        // </HACK>

    }

    private void unabstractSelectedItem (ListItem item) {

        // Return if the item is not a complex item.
        if (item.type != CustomAdapter.COMPLEX_LAYOUT) {
            return;
        }

        int index = 0;

        // Get list of the abstracted items
        ArrayList<ListItem> abstractedListItems = item.listItems;

        // Get position of the selected item
        index = data.indexOf(item);

        // Remove the selected item from the list (it will be replaced by the abstracted behviors)
        data.remove (index);
        updateViewFromData(); // Update view after removing items from the list

        // Add the abstracted items back to the list
        for (ListItem listItem : abstractedListItems) {
            data.add (index, listItem);
            index++; // Increment the index of the insertion position
        }
        updateViewFromData(); // Update view after removing items from the list

    }

    public View getViewByPosition (int xPosition, int yPosition) {
        View mDownView = null;
        // Find the child view that was touched (perform a hit test)
        Rect rect = new Rect();
        int childCount = this.getChildCount();
        int[] listViewCoords = new int[2];
        this.getLocationOnScreen(listViewCoords);
        int x = (int) xPosition - listViewCoords[0];
        int y = (int) yPosition - listViewCoords[1];
        View child;
        int i = 0;
        for ( ; i < childCount; i++) {
            child = this.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                mDownView = child; // This is your down view
                break;
            }
        }

        return mDownView;
    }

    public int getViewIndexByPosition (int xPosition, int yPosition) {
        View mDownView = null;
        // Find the child view that was touched (perform a hit test)
        Rect rect = new Rect();
        int childCount = this.getChildCount();
        int[] listViewCoords = new int[2];
        this.getLocationOnScreen(listViewCoords);
        int x = (int) xPosition - listViewCoords[0];
        int y = (int) yPosition - listViewCoords[1];
        View child;
        int i = 0;
        for ( ; i < childCount; i++) {
            child = this.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                mDownView = child; // This is your down view
                break;
            }
        }

        // Check if the specified position is within the bounds of a view in the ListView.
        // If so, select the item.
        if (mDownView != null) {
            int itemIndex = this.getFirstVisiblePosition() + i;
            return itemIndex;
        }

        return -1;
    }

    public void selectItemByIndex (int index) {
        // Check if the specified position is within the bounds of a view in the ListView.
        // If so, select the item.
//        if (mDownView != null) {
//            int itemIndex = this.getFirstVisiblePosition() + i;
            if (index < data.size()) {
                ListItem item = (ListItem) data.get(index);
                selectListItem(item);
                updateViewFromData();
            }
//        }
    }

//    public ListItem getItemByView (View view) {
//        int position = this.getPositionForView(view);
//        return (ListItem) data.get(position);
//    }
}
