package com.example.odd.ostrinofragnavdrawer;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.odd.ostrinofragnavdrawer.Listeners.PlayerListener;
import com.example.odd.ostrinofragnavdrawer.Listeners.QueueListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.R.attr.orientation;

public class ListFragment extends Fragment implements FunnyJunk.YareYareListener,
        View.OnClickListener, QueueListener, PlayerListener {

    private boolean editedOst;
    private int ostReplaceId, previouslyPlayed;
    private List<Ost> allOsts, currOstList;
    private DBHandler dbHandler;
    private EditText filter;
    private String TAG = "OstInfo";
    private String TAG2 = "Jojo";
    private String filterText;
    private ImageButton btnShuffle;
    private CustomAdapter customAdapter;
    private ListView lvOst;
    boolean shuffleActivated, playerDocked;
    private AddScreen dialog;
    private Ost unAddedOst;
    private MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        shuffleActivated = false;
        playerDocked = true;
        final View rootView = inflater.inflate(R.layout.activity_listscreen, container, false);
        dbHandler = new DBHandler(getActivity());
        unAddedOst = null;

        lvOst = (ListView) rootView.findViewById(R.id.lvOstList);
        lvOst.findViewById(R.id.btnOptions);
        lvOst.setDivider(null);

        allOsts = dbHandler.getAllOsts();

        customAdapter = new CustomAdapter(getContext(), allOsts, this);
        lvOst.setAdapter(customAdapter);

        lvOst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currOstList = getCurrDispOstList();
                mainActivity.initiatePlayer(currOstList, position);
                if(previouslyPlayed >= 0 && previouslyPlayed < currOstList.size()){
                    getViewByPosition(previouslyPlayed, lvOst).setBackgroundResource(R.drawable.white);
                }
                view.setBackgroundResource(R.drawable.greenrect);
                customAdapter.updateCurrentlyPlaying(position);
                previouslyPlayed = customAdapter.getNowPlaying();
            }
        });

        lvOst.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                currOstList = getCurrDispOstList();
                Ost ost = getCurrDispOstList().get(position);
                dialog = new AddScreen();
                dialog.show(getFragmentManager(), TAG);
                ostReplaceId = ost.getId();
                dialog.setText(ost);
                dialog.setButtonText("Save");
                dialog.showDeleteButton();
                editedOst = true;
                return true;
            }
        });

        filter = (EditText) rootView.findViewById(R.id.edtFilter);
        filterText = "";
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filterString = s.toString();
                customAdapter.filter(filterString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                filterText = filter.getText().toString();
                filterText = filterText.toLowerCase();
                if (filterText.equals("muda muda muda")) {
                    FunnyJunk dialog = new FunnyJunk();
                    dialog.show(getActivity().getFragmentManager(), TAG2);
                }
                customAdapter.filter(s.toString());
            }
        };
        filter.addTextChangedListener(textWatcher);
        btnShuffle = (ImageButton)  rootView.findViewById(R.id.btnShuffle);
        ImageButton btnShufflePlay = (ImageButton) rootView.findViewById(R.id.btnShufflePlay);
        ImageButton btnAdd = (ImageButton)  rootView.findViewById(R.id.btnAdd);

        btnShuffle.setOnClickListener(this);
        btnShufflePlay.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnShuffle:{
                if(!mainActivity.youtubeFragNotLaunched()){
                    Toast.makeText(getActivity(), "Player is not running", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(shuffleActivated){
                    shuffleActivated = false;
                    mainActivity.shuffleOff();
                    btnShuffle.setBackgroundResource(R.drawable.shuffle);
                }
                else{
                    shuffleActivated = true;
                    mainActivity.shuffleOn();
                    btnShuffle.setBackgroundResource(R.drawable.shuffle_activated);
                }
                break;

            }
            case R.id.btnShufflePlay:{
                Random rnd = new Random();
                currOstList = getCurrDispOstList();
                int rndPos = rnd.nextInt(currOstList.size());
                mainActivity.initiatePlayer(currOstList, rndPos);
                customAdapter.updateCurrentlyPlaying(rndPos);
                refreshListView();
                mainActivity.shuffleOn();
                btnShuffle.setBackgroundResource(R.drawable.shuffle_activated);
                shuffleActivated = true;
                previouslyPlayed = rndPos;
                break;
            }

            case R.id.btnAdd:{
                dialog = new AddScreen();
                System.out.println(unAddedOst);
                if(unAddedOst != null){
                    dialog.setText(unAddedOst);
                }
                dialog.show(getFragmentManager(), TAG);
                dialog.setButtonText("Add");
                editedOst = false;
                break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        System.out.println(playerDocked);
    }

    public int getOstReplaceId(){
        return ostReplaceId;
    }

    public boolean isEditedOst(){
        return editedOst;
    }

    public List<Ost> getCurrDispOstList(){
        if(filterText.equals("")){
            return allOsts;
        }
        List<Ost> currOsts = new ArrayList<>();
        for( Ost ost : allOsts){
            String ostString = ost.getTitle() + ", " + ost.getShow() + ", " + ost.getTags();
            ostString = ostString.toLowerCase();
            if(ostString.contains(filterText)){
                currOsts.add(ost);
            }
        }
        return currOsts;
    }

    @Override
    public void addToQueue(int addId) {
        Ost ost = getCurrDispOstList().get(addId);
        mainActivity.addToQueue(ost);
    }

    @Override
    public void removeFromQueue(String url) {

    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    public void updateCurrentlyPlaying(int newId){
        getViewByPosition(newId, lvOst).setBackgroundResource(R.drawable.greenrect);
        getViewByPosition(previouslyPlayed, lvOst).setBackgroundResource(R.drawable.white);
        previouslyPlayed = newId;
    }

    @Override
    public void next() {

    }

    @Override
    public void previous() {

    }

    @Override
    public void shuffle(List<Ost> ostList) {

    }

    public void refreshListView(){
        editedOst = false;
        allOsts = dbHandler.getAllOsts();
        currOstList = getCurrDispOstList();
        customAdapter.updateList(currOstList);
    }

    public AddScreen getDialog(){
        return dialog;
    }

    public void setUnAddedOst(Ost ost){
        unAddedOst = ost;
    }


    public void setMainAcitivity(MainActivity mainAcitivity){
        this.mainActivity = mainAcitivity;
    }

    public CustomAdapter getCustomAdapter() {
        return customAdapter;
    }
}
