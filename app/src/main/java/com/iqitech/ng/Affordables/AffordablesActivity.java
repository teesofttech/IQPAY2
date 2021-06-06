package com.iqitech.ng.Affordables;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.iqitech.ng.Adapter.ServiceVendorAdapter;
import com.iqitech.ng.Models.ServiceVendor;
import com.iqitech.ng.Models.UserModel;
import com.iqitech.ng.Models.VendorModel;
import com.iqitech.ng.Models.vendModel;
import com.iqitech.ng.R;
import com.iqitech.ng.Utils.AlertDialogManager;
import com.iqitech.ng.Utils.Constant;
import com.iqitech.ng.Utils.PrefUtils;
import com.iqitech.ng.agents.PreviewActivity;
import com.iqitech.ng.app.AppController;
import com.libizo.CustomEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class AffordablesActivity extends AppCompatActivity {
    private static final String TAG = AffordablesActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<ServiceVendor> cartList;
    private ServiceVendorAdapter mAdapter;
    private List<VendorModel> vendorModelList;
    //String URL;
    private SearchView search;
    ProgressDialog progressDialog;
    String config_url;
    //AwesomeSpinner my_spinner,my_spinner2;
    String item1, item2;
    String VendId;

    ArrayList<String> vendorname;
    ArrayList<vendModel> ServiceType;
    // ArrayList<String> vendorname;
    //ArrayList<String> ServiceType;
    AlertDialogManager alertDialogManager;
    ImageView image;
    Spinner materialSpinner, materialSpinnerType, deliveryPoint, CollectionPoint;
    UserModel model;
    String collect = "";
    String delivery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affordables);

        final ActionBar abar = getSupportActionBar();
        View viewActionBar = getLayoutInflater().inflate(R.layout.custom_bar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        TextView txttitle = (TextView) viewActionBar.findViewById(R.id.txtTitle);
        txttitle.setText("A F F O R D A B L E   F A R M S");
        txttitle.setTextSize(14);
        if (abar != null) {
            abar.setCustomView(viewActionBar, params);

            abar.setDisplayShowCustomEnabled(true);
            abar.setDisplayShowTitleEnabled(false);
            abar.setHomeButtonEnabled(false);
        }
        ArrayList arrayList = new ArrayList<>();
        arrayList.add("Collection Point");
        arrayList.add("Courier / Delivery");

        ArrayList arrayList1 = new ArrayList<>();
        arrayList1.add("Agege");
        arrayList1.add("Berger");
        arrayList1.add("Oshodi");

        com.libizo.CustomEditText amountText = findViewById(R.id.amount);

        alertDialogManager = new AlertDialogManager();
        image = findViewById(R.id.image);
        ACProgressFlower dialog = new ACProgressFlower.Builder(AffordablesActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Please wait...")
                .fadeColor(Color.DKGRAY).build();

        model = PrefUtils.getCurrentUser(AffordablesActivity.this);
        materialSpinner = (Spinner) findViewById(R.id.material_spinner_1);
        deliveryPoint = (Spinner) findViewById(R.id.deliveryPoint);
        CollectionPoint = (Spinner) findViewById(R.id.collectionPoint);
        materialSpinnerType = (Spinner) findViewById(R.id.material_spinner_type);
        config_url = Constant.GET_CATEGORIES + "8/" + "vendors";
        Log.d("url", config_url);
        cartList = new ArrayList<>();
        vendorname = new ArrayList<>();
        ServiceType = new ArrayList<>();
        CustomEditText phonenumber = findViewById(R.id.phonenumber);
        CustomEditText customername = findViewById(R.id.customername);
        CustomEditText amount = findViewById(R.id.amount);

        progressDialog = new ProgressDialog(AffordablesActivity.this);
        fetchRecipes(config_url);
        materialSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item1 = materialSpinner.getSelectedItem().toString();
                ServiceType.clear();
                Log.d("serviceVendor", String.valueOf(cartList.size()));
                Log.d("item", item1);
                for (ServiceVendor vendor : cartList) {
                    if (vendor.getVendorName().equals(item1)) {
                        vendModel vend = new vendModel(String.valueOf(vendor.getId()), String.valueOf(vendor.getName()), String.valueOf(vendor.getAmount()));
                        ServiceType.add(vend);
                    }
                }
                ArrayAdapter<vendModel> adapter = new ArrayAdapter<vendModel>(AffordablesActivity.this,
                        android.R.layout.simple_spinner_item, ServiceType);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                materialSpinnerType.setAdapter(adapter);

                materialSpinnerType.setPrompt("Select one!");

                if (item1.equals("Affordable Farms")) {
                    image.setImageResource(R.mipmap.ramm);
                }
                if (item1.equals("Goat")) {
                    image.setImageResource(R.mipmap.goatt);
                }
                if (item1.equals("Cow")) {
                    image.setImageResource(R.mipmap.coww);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        materialSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vendModel user = (vendModel) materialSpinnerType.getSelectedItem();
                // item2 = user.getId(); //materialSpinnerType.getSelectedItem().toString();
                VendId = user.getId();
                item2 = materialSpinnerType.getSelectedItem().toString();
                Log.d("category", String.valueOf(user.getName()));
                Log.d("category1", String.valueOf(user.getId()));
                Log.d("category2", String.valueOf(user.getAmount()));

                Log.d("category3", String.valueOf(item2));

                if (user.getName().equals(item2)) {
                    //  ServiceType.add(vendor.getAmount());
                    amount.setText(String.valueOf(user.getAmount()));
                    //Log.d("serviceVendor1", String.valueOf(vendor.getVendorName()));
                }
//
//                Log.d("serviceVendor", String.valueOf(cartList.size()));
//                for (ServiceVendor vendor : cartList) {
//                    if (vendor.getVendorName().equals(item2)) {
//                        //ServiceType.add(vendor.getName());
//                    }
//                }
                // Toast.makeText(getApplicationContext(), vendor.getId(), Toast.LENGTH_LONG).show();
                // Log.d("ID", String.valueOf(vendor.getId()));
                // materialSpinnerType.setAdapter(new ArrayAdapter<String>(ElectricityActivity.this, android.R.layout.simple_spinner_dropdown_item, ServiceType));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deliveryPoint.setAdapter(arrayAdapter);

        deliveryPoint.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                delivery = String.valueOf(deliveryPoint.getSelectedItem());
                if (deliveryPoint.getSelectedItem().equals("Collection Point")) {
                    delivery = "1";
                }
                if (deliveryPoint.getSelectedItem().equals("Courier / Delivery")) {
                    delivery = "2";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        materialSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vendModel user = (vendModel) materialSpinnerType.getSelectedItem();
                // item2 = user.getId(); //materialSpinnerType.getSelectedItem().toString();
                VendId = user.getId();
                item2 = materialSpinnerType.getSelectedItem().toString();
                Log.d("category", String.valueOf(user.getName()));
                Log.d("category1", String.valueOf(user.getId()));
                Log.d("category2", String.valueOf(user.getAmount()));

                Log.d("category3", String.valueOf(item2));

                if (user.getName().equals(item2)) {
                    //  ServiceType.add(vendor.getAmount());
                    amount.setText(String.valueOf(user.getAmount()));
                    //Log.d("serviceVendor1", String.valueOf(vendor.getVendorName()));
                }
//
//                Log.d("serviceVendor", String.valueOf(cartList.size()));
//                for (ServiceVendor vendor : cartList) {
//                    if (vendor.getVendorName().equals(item2)) {
//                        //ServiceType.add(vendor.getName());
//                    }
//                }
                // Toast.makeText(getApplicationContext(), vendor.getId(), Toast.LENGTH_LONG).show();
                // Log.d("ID", String.valueOf(vendor.getId()));
                // materialSpinnerType.setAdapter(new ArrayAdapter<String>(ElectricityActivity.this, android.R.layout.simple_spinner_dropdown_item, ServiceType));

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter arrayAdapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayList1);
        arrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        CollectionPoint.setAdapter(arrayAdapter1);

        CollectionPoint.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                collect = String.valueOf(CollectionPoint.getSelectedItem());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        Button btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String _phonenumber = phonenumber.getText().toString();
                String Amount = amount.getText().toString();

                dialog.show();

                if (phonenumber.getText().toString().equals("") && amount.getText().toString().equals("")) {
                    alertDialogManager.showAlertDialog(AffordablesActivity.this, "Error", "Please fill up the empty field (s)", false);
                } else {
                    JSONObject params = new JSONObject();

                    try {
                        /*accountName: "tes"
amount: "900.00"
collectionPoint: "Agege"
customerNumber: "123569"
deliveryAddressLine1: "333 FREMONT STREET"
deliveryAddressLine2: null
deliveryOption: "1"
lga: "2"
numberOfPins: 1
serviceId: 171
state: "25"*/
                        params.put("accountName", customername.getText().toString());
                        params.put("customerNumber", phonenumber.getText().toString());
                        params.put("amount", Amount.trim());
                        params.put("collectionPoint", "Agege");
                        params.put("deliveryAddressLine1", "333 FREMONT STREET");
                        params.put("deliveryAddressLine2", "");
                        params.put("deliveryOption", delivery);
                        params.put("lga", "2");
                        params.put("numberOfPins", "1");
                        params.put("serviceId", VendId);
                        params.put("state", "25");
                        Log.d("yy", params.toString()); // params.put("updateVateepProfile", "true");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Make request for JSONObject
                    //vending/initiate
                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                            Request.Method.POST, Constant.VENDING, params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("RESPONSE", response.toString());
                                    dialog.dismiss();
                                    try {
                                        String statusCode = response.getString("statusCode");
                                        Log.d("stat", statusCode);
                                        if (statusCode.equals("200")) {
                                            //alertDialogManager.showAlertDialog(RechargeVendorsActivity.this, "Success", response.getString("message"), true);

                                            Toast.makeText(AffordablesActivity.this, response.getString("message"), Toast.LENGTH_LONG).show();

                                            JSONObject content = response.getJSONObject("content");
                                            Intent ii = new Intent(AffordablesActivity.this, PreviewActivity.class);
                                            ii.putExtra("vendingCode", content.getString("vendingCode"));
                                            ii.putExtra("status", content.getString("status"));
                                            ii.putExtra("service", content.getString("service"));
                                            ii.putExtra("accountName", content.getString("accountName"));
                                            ii.putExtra("type", content.getString("type"));
                                            ii.putExtra("pin", content.getString("pin"));
                                            ii.putExtra("amount", content.getString("amount"));
                                            ii.putExtra("quantity", content.getString("quantity"));
                                            ii.putExtra("customerNumber", content.getString("customerNumber"));
                                            ii.putExtra("serviceId", content.getString("serviceId"));
                                            ii.putExtra("id", content.getString("id"));
                                            ii.putExtra("vendorLogo", content.getString("vendorLogo"));
                                            startActivity(ii);

                                        } else {
                                            alertDialogManager.showAlertDialog(AffordablesActivity.this, "Failed", "Registration failed", false);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            dialog.dismiss();
                            VolleyLog.d("TAGGG", "Error: " + error.getStackTrace());
                        }
                    }) {

                        /**
                         * Passing some request headers
                         */
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type", "application/json; charset=utf-8");
                            headers.put("Authorization", "Bearer " + model.getToken());
                            Log.d("TAG", "getHeaders: " + headers.toString());
                            return headers;
                        }
                    };

                    DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    jsonObjReq.setRetryPolicy(retryPolicy);
                    AppController.getInstance().addToRequestQueue(jsonObjReq);

                }
            }
        });
    }

    private void fetchRecipes(String URL) {
        progressDialog.setMessage("Loading");
        progressDialog.show();
        cartList = new ArrayList<>();
        cartList.clear();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                try {
                    // Parsing json object response
                    // response will be a json object
                    String name = response.getString("message");
                    String email = response.getString("statusCode");
                    JSONArray content = response.getJSONArray("content");
                    for (int i = 0; i < content.length(); i++) {

                        JSONObject vend = (JSONObject) content
                                .get(i);

                        VendorModel vendor = new VendorModel();
                        vendor.setId(vend.getInt("id"));
                        vendor.setName(vend.getString("name"));
                        Log.d("vendrname", vend.getString("name"));
                        vendorname.add(vend.getString("name"));
                        materialSpinner.setAdapter(new ArrayAdapter<String>(AffordablesActivity.this, android.R.layout.simple_spinner_dropdown_item, vendorname));

                        JSONArray services = vend.getJSONArray("serivces");

                        for (int k = 0; k < services.length(); k++) {

                            try {
                                final JSONObject obj = (JSONObject) services
                                        .get(k);

                                ServiceVendor mydic = new ServiceVendor();
                                mydic.setId(obj.getInt("id"));
                                mydic.setVendorName(obj.getString("vendorName"));
                                mydic.setName(obj.getString("name"));
                                mydic.setAmount(obj.getDouble("amount"));
                                mydic.setDescription(obj.getString("description"));
                                cartList.add(mydic);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //  mAdapter.notifyDataSetChanged();

                    }
                    // stop animating Shimmer and hide the layout

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AffordablesActivity.this, "Error occurred while vending", Toast.LENGTH_LONG).show();
                VolleyLog.d("TAGGG", "Error: " + error.getMessage());
                // As of f605da3 the following should work
                progressDialog.dismiss();
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONObject obj = new JSONObject(res);
                        Log.d("error", obj.getString("message"));
                        Toast.makeText(AffordablesActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();

                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                    }
                }

            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Authorization", "Bearer " + model.getToken());
                Log.d("TAG", "getHeaders: " + headers.toString());
                return headers;
            }
        };

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjReq.setRetryPolicy(retryPolicy);
        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }
}