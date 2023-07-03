package com.resourcity.naama.resourcity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public final static int REQ_CODE_ADD_RESOURCE_CHILD = 1;
    public final static int RESULT_CANCEL_RESOURCE = 0;
    private static final String ADD_RESOURCE_TITLE = "ADD_RESOURCE";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static  final String SELECT_TITLE = "SELECT FILTER";

    private EditText mSearchText;
    private ImageView mGpsLocate;
    private ImageView mMainMenu;
    private ImageView mFilters;

//    private HashMap<String, Boolean> mActiveFilters;

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker mMarker;
    private HashMap<String, Boolean> mFilteredTags;

    private ResourcePreviewFragment mPreviewFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mFragmentTransaction;

    // INIT FROM THE USER ENTERED THROUGHT THE MAIN ACTIVITY!
    private String mUserEmail;

    private Menu mMenu;

    private static final float DEFAULT_ZOOM = 15f;
//    private static final int PLACE_PICKER_REQUEST = 1;
//    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
//            new LatLng(-40, -168), new LatLng(71, 136));

    // FIREBASE
    private FirebaseAuth firebaseAuth;
    DatabaseReference resourceDatabase;
    DatabaseReference noteDatabase;
    StorageReference storageReference;

    //public ArrayList<ResourceNote> mNoteList;
    private ArrayList<Resource> mResourceList;
    private Resource mSelectedResource;

    public static ArrayList<ResourceNote> mNoteList = new ArrayList<ResourceNote>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mResourceList = new ArrayList<Resource>();
        activateFilters();

        mSearchText = (EditText) findViewById(R.id.input_search);
        mGpsLocate = (ImageView) findViewById(R.id.go_to_my_location);
        mMainMenu = (ImageView) findViewById(R.id.ic_menu);
        mFilters = (ImageView) findViewById(R.id.check_filters);

        firebaseAuth = FirebaseAuth.getInstance();
        getLocationPermission();

        resourceDatabase = FirebaseDatabase.getInstance().getReference("resources");
        noteDatabase = FirebaseDatabase.getInstance().getReference("notes");
        storageReference = FirebaseStorage.getInstance().getReference("resources");

        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();

        //TODO: tocheck
        mUserEmail = (String) getIntent().getSerializableExtra("operatingUser");

    }

    private void activateFilters()
    {
        mFilteredTags = new HashMap<String, Boolean>();
        String[] filtersNames = {"FOOD", "HERBS", "METAL", "WOOD", "PLASTIC", "CLOTHES", "FURNITURE"};
        for (String filter : filtersNames)
        {
            mFilteredTags.put(filter, true);
        }
    }

    private void init(){
        Log.d(TAG, "init: initializing");

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_NEXT
                        || actionId == EditorInfo.IME_ACTION_SEND
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }
                return false;
            }
        });

        mGpsLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

        mMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ManageMainMenu();
            }
        });

        mFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ManageFiltersMenu();
            }
        });
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void ManageMainMenu(){
        PopupMenu mainMenu = new PopupMenu(MapActivity.this, mMainMenu);
        mainMenu.getMenuInflater().inflate(R.menu.main_menu, mainMenu.getMenu());

        mainMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //acoording to item choose what to do...
                String itemName = item.getTitle().toString();
                switch (itemName)
                {
                    case "Info":
                        Intent intent_info = new Intent(MapActivity.this, InfoActivity.class);
                        startActivity(intent_info);
                        break;
                    case "Your Profile":
                        Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
                        // TODO: put extra...
                        startActivity(intent);
                        break;
                    case "Log Out":
                        logOutUser();
                        break;
                    default:
                        System.out.println("invalid tag");
                        return false;
                }
                return true;
            }
        });
        mainMenu.show();
    }

    private int MenuIdx(MenuItem item){
        for (int i = 0; i<mMenu.size(); i++){
            if (mMenu.getItem(i) == item){
                return i;
            }
        }
        return -1;
    }

    private void ManageFiltersMenu(){
        // present a new fragment with menue of the filters and checkbox for that.
        PopupMenu filtersMenu = new PopupMenu(MapActivity.this, mFilters);
        filtersMenu.getMenuInflater().inflate(R.menu.filters_menu, filtersMenu.getMenu());

        mMenu = filtersMenu.getMenu();
        // start all items checked.
        for (int i = 0; i < mMenu.size(); i++)
        {
            MenuItem item = mMenu.getItem(i);

            if (! item.toString().equals(SELECT_TITLE))
            {
                if (mFilteredTags.get(item.toString()).booleanValue()) {
                    item.setChecked(true);
                }
                else
                {
                    item.setChecked(false);
                }
            }
        }

        filtersMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String itemName = item.getTitle().toString();

                // Change check to uncheck and the opposite
                if (! itemName.equals(SELECT_TITLE)) {
                    item.setChecked(!item.isChecked());
                    boolean newVal = !mFilteredTags.get(itemName);
                    mFilteredTags.put(itemName, newVal);
                    showMarkers();
                    //Resource.ResourceTag itemTag = Resource.ResourceTag.valueOf(itemName);
                }

                // Keep the popup menu open
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                item.setActionView(new View(MapActivity.this));
                item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return false;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        return false;
                    }
                });
                return false;
            }
        });
        filtersMenu.show();
    }

    private void logOutUser()
    {
        if (firebaseAuth.getCurrentUser() != null)
        {
            firebaseAuth.signOut();
            if (firebaseAuth.getCurrentUser() == null)
            {
                Toast.makeText(this, "Successfully logged out", Toast.LENGTH_SHORT).show();
            }
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // This could be moved into an abstract BaseActivity
    // class for being re-used by several instances
    protected void replacePreviewFragment(android.support.v4.app.Fragment fragment) {
        // content is the root view of this activity
        mFragmentTransaction.replace(android.R.id.content, fragment);
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
    }

    // This could be moved into an abstract BaseActivity
    // class for being re-used by several instances
    protected void addPreviewFragment(android.support.v4.app.Fragment fragment) {
        mFragmentTransaction.add(android.R.id.content, fragment);
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
    }

    protected void hidePreviewFragment(){
        FrameLayout frameLayout_preview_container = (MapActivity.this).findViewById(R.id.resource_preview_fragment_container);
        frameLayout_preview_container.setVisibility(View.INVISIBLE);

        mFragmentTransaction.hide((ResourcePreviewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.resource_preview_fragment));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: mas is ready");
        if (mFilteredTags == null)
        {
            activateFilters();
        }
        mMap = googleMap;
        putPinsOnMap();
        updateNotes();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // ADD NEW RESOURCE
                if (marker.getTitle().equals(ADD_RESOURCE_TITLE)) {
                    mMarker.remove();

                    Intent intentChild = new Intent(MapActivity.this, AddResourceActivity.class);
                    intentChild.putExtra("result_code", REQ_CODE_ADD_RESOURCE_CHILD);
                    intentChild.putExtra("cancel", RESULT_CANCEL_RESOURCE);
                    intentChild.putExtra("lat", marker.getPosition().latitude);
                    intentChild.putExtra("lng", marker.getPosition().longitude);
                    intentChild.putExtra("operatingUser", mUserEmail);
                    startActivityForResult(intentChild, REQ_CODE_ADD_RESOURCE_CHILD);
                }

                // VIEW EXISTING RESOURCE
                else {
                    // TODO: take marker's information and use it in "viewResourceActivity"

                    LatLng resourceLocation = marker.getPosition();
                    mSelectedResource = FindResourceByLatLng(resourceLocation);
                    if (mSelectedResource != null)
                    {
//                        //if it is an activity
                        Intent intentChild = new Intent(MapActivity.this, ViewResourceActivity.class);
                        intentChild.putExtra("resourceData", mSelectedResource);
                        intentChild.putExtra("operationUserEmail", mUserEmail);
                        startActivity(intentChild);

                        ArrayList<String> ImageUrls = mSelectedResource.getResourcePicturesUrlList();
                        if ( ! ImageUrls.isEmpty()){
                            String firstImgUrl = ImageUrls.get(0);
                            Bitmap bitmapImg = getImageBitmap(firstImgUrl);
                            ImageView resourceImgPreview = (MapActivity.this).findViewById(R.id.imagePreview);
                            resourceImgPreview.setImageBitmap(bitmapImg);
                        }

                        String resourceTitle = mSelectedResource.getName();
                        TextView resourceTitleView = (MapActivity.this).findViewById(R.id.titleTxtView);
                        resourceTitleView.setText(resourceTitle);

//                        FrameLayout frameLayout_preview_container = (MapActivity.this).findViewById(R.id.resource_preview_fragment_container);
//                        frameLayout_preview_container.setVisibility(View.VISIBLE);

                        //Fragment fragment = ResourcePreviewFragment.newInstance(mSelectedResource);
                        ResourcePreviewFragment fragment = new ResourcePreviewFragment();
                        fragment.setResource(mSelectedResource);

//                        mFragmentTransaction.replace(R.id.resource_preview_fragment, fragment);
//                        mFragmentTransaction.commit();
                    }
                }
            return false;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Toast.makeText(MapActivity.this, "long press", Toast.LENGTH_SHORT).show();
                //create temporary add pin
                addResourceMarker(latLng);
            }
        });

        hidePreviewFragment();

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

            init();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mLocationPermissionsGranted = false;
        Log.d(TAG, "onRequestPermissionsResult: called");
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0){
                    for (int i =0; i<grantResults.length; i++) {
                        if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    // init the map
                    initMap();
//                    if (googleServicesAvailable()) {
//                        Toast.makeText(this, "Connected to play services", Toast.LENGTH_LONG).show();
//                        setContentView(R.layout.activity_main);
//                        initMap();
//                    } else {
//                        Toast.makeText(this, "Not connected", Toast.LENGTH_LONG).show();
//                    }
                }
            }
        }
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        final boolean isGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        try{
            if(mLocationPermissionsGranted){

                final Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful() && isGPSEnabled){
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();

                            if (currentLocation == null)
                            {
                                Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                                currentLocation = new Location("");
                                currentLocation.setLatitude(34.7818501);
                                currentLocation.setLongitude(32.0831488);
                            }

//                            List<Resource> resources = MainActivity.resourceDataBase.dao().getResourcesList();
//                            for (Resource resource : resources)
//                            {
//                                addMarker(resource);
//                            }

                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, ADD_RESOURCE_TITLE);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void addResourceMarker(LatLng latLng)
    {
        BitmapDescriptor pinIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_add_resource_pin);
        // Changing marker icon to "add icon"
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .icon(pinIcon)
                .title(ADD_RESOURCE_TITLE);

        //Bitmap bit = BitmapFactory.decodeFile(String.valueOf("drawable\addResource"));
        if (mMarker != null){
            mMarker.remove();
        }
        mMarker = mMap.addMarker(markerOptions);
    }

    private void addMarker(Resource resource)
    {
        MarkerOptions markerOptions;
        // divide the marker image by categories (tag: cloth, food..)

        Resource.ResourceTag bitMapTag = resource.getTagEnum();
        String tagName = bitMapTag.toString();
        if (mMarker != null){
            mMarker.remove();
        }
        // FOOD, HERBS, METAL, WOOD, PLASTIC, CLOTH, FURNITURE
        Marker newMarker = null;
        // TODO: switch case on enum
        switch(bitMapTag){
            case FOOD:
                // food
                markerOptions = new MarkerOptions().position(resource.getLatLng())
                        .title(resource.getTagEnum().name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_food_pin));
                newMarker = mMap.addMarker(markerOptions);
                break;
            case METAL:
                // metal
                markerOptions = new MarkerOptions().position(resource.getLatLng())
                        .title(resource.getTagEnum().name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_metal_pin));
                newMarker = mMap.addMarker(markerOptions);
                break;
            case WOOD:
                //R.array.filterArray
                markerOptions = new MarkerOptions().position(resource.getLatLng())
                        .title(resource.getTagEnum().name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_wood_pin));
                newMarker = mMap.addMarker(markerOptions);
                break;
            case PLASTIC:
                //plastic
                markerOptions = new MarkerOptions().position(resource.getLatLng())
                        .title(resource.getTagEnum().name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_plastic_pin));
                newMarker = mMap.addMarker(markerOptions);
                break;
            case CLOTHES:
                //cloth
                markerOptions = new MarkerOptions().position(resource.getLatLng())
                        .title(resource.getTagEnum().name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cloth_pin));
                newMarker = mMap.addMarker(markerOptions);
                break;
            case FURNITURE:
                //furniture
                markerOptions = new MarkerOptions().position(resource.getLatLng())
                        .title(resource.getTagEnum().name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_furniture_pin));
                mMap.addMarker(markerOptions);
                break;
            case HERBS:
                //furniture
                markerOptions = new MarkerOptions().position(resource.getLatLng())
                        .title(resource.getTagEnum().name())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_herbs_pin));
                newMarker = mMap.addMarker(markerOptions);
                break;
            default:
                System.out.println("invalid tag");
                break;
        }
    }

    private void moveCamera(LatLng latLng, float zoom , String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        addResourceMarker(latLng);
        //hideSoftKeyboard();
    }

//    private void hideSoftKeyboard(){
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//    }

//    private void goToLocation(double lat, double lng) {
//        LatLng ll = new LatLng(lat, lng);
//        CameraUpdate update = CameraUpdateFactory.newLatLng(ll);
//        mMap.moveCamera(update);
//
//    }

//    private void goToLocationZoom(double lat, double lng, float zoom) {
//        LatLng ll = new LatLng(lat, lng);
//        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
//        mMap.moveCamera(update);
//    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void geoLocate() {

        Log.d(TAG, "geoLocate: geolocating");
        if (mMarker != null){
            mMarker.remove();
        }
        hideKeyboard(this);

        String searchString = mSearchText.getText().toString();

        mSearchText.getText().clear();

        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }
//        if (mMarker != null){
//            mMarker.remove();
//        }
//
//        hideKeyboard(this);
//        EditText et = (EditText) findViewById(R.id.input_search);
//        String location = et.getText().toString();
//        et.getText().clear();
//
//        Geocoder geoCoder = new Geocoder(this);
//        try {
//            List<Address> address_list = geoCoder.getFromLocationName(location, 1);
//
//            Address address = address_list.get(0);
//            String locality = address.getLocality();
//
//            double lat = address.getLatitude();
//            double lng = address.getLongitude();
//
//            goToLocationZoom(lat, lng, 15);
//
//            MarkerOptions markerOptions = new MarkerOptions()
//                    .title(locality)
//                    .position(new LatLng(lat, lng));
//            mMarker = mMap.addMarker(markerOptions);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void openViewResource(View view) throws IOException {
        hideKeyboard(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        //TODO: Should NOT be annotated
//        //requestLocationUpdates(LocationRequest var1, LocationCallback var2, @Nullable Looper var3)
//        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, mLo);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null){
            Toast.makeText(this, "Can't access location", Toast.LENGTH_LONG).show();
        }
        else {
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
            mMap.animateCamera(update);
        }
    }

//    public void add_user(View view)
//    {
//        fragmentManager = getSupportFragmentManager();
//
//        if (findViewById(R.id.join_button) != null)
//        {
//            fragmentManager.beginTransaction().add(R.id.join_screen, new JoinFragment()).commit();
//        }
//    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailble = api.isGooglePlayServicesAvailable(this);
        if (isAvailble == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailble)) {
            Dialog dialog = api.getErrorDialog(this, isAvailble, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to google play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {

        //RESULT_CANCEL_RESOURCE ???

        if (requestCode == REQ_CODE_ADD_RESOURCE_CHILD) {
            Resource resourceToAdd = (Resource) data.getExtras().getSerializable("resource_object");
            // TODO: to check
            Toast.makeText(this, "Resource Successfully Added", Toast.LENGTH_SHORT).show();

            // Manage markers
            mMarker.remove();
            addMarker(resourceToAdd);


            // TODO: Adi- Add resource to Firebase
        }
    }

    private void VisiblePinsFilter(ArrayList<Resource.ResourceTag> typeToShow){
        // marker.setVisible(false);
    }

    @Override
    public void onBackPressed() {
        // wanna log out??

        //
        //logOutUser();
    }

//    private void updateMarkers()
//    {
//        putPinsOnMap();
//        for (Resource resource :resourceList)
//        {
//            addMarker(resource);
//        }
//    }

    private void putPinsOnMap()
    {
        resourceDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                mResourceList.clear();
                for (DataSnapshot resourceSnapshot : dataSnapshot.getChildren())
                {
                    Resource resource = resourceSnapshot.getValue(Resource.class);
                    mResourceList.add(resource);
                }
                showMarkers();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void updateNotes()
    {
        noteDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mNoteList.clear();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren())
                {
                    ResourceNote note = noteSnapshot.getValue(ResourceNote.class);
                    mNoteList.add(note);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}

        });
    }

    private Resource FindResourceByLatLng(LatLng pos) {
        for (int i = 0; i < mResourceList.size(); i++) {
            if ((float) pos.latitude == mResourceList.get(i).getLat() &&
                    (float) pos.longitude == mResourceList.get(i).getLng()) {

                return mResourceList.get(i);
            }
        }
        return null;
    }

    private void showMarkers()
    {
        mMap.clear();
        for (Resource resource :mResourceList)
        {
            if((resource.mName != null) && (mFilteredTags.get(resource.mTagEnum.toString())))
            {
                addMarker(resource);
            }
        }
    }

    public static Bitmap getImageBitmap(String urlString)
    {
        Bitmap bmp = null;
        try {
            bmp = new DownloadImageTask().execute(urlString).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return bmp;
    }


    public static class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

        private Exception exception;

        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                return bmp;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(Bitmap result)
        {
        }
    }
}
