package com.iqitech.ng.Affordables;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.bumptech.glide.Glide;
import com.iqitech.ng.Models.UserModel;
import com.iqitech.ng.R;
import com.iqitech.ng.Utils.AlertDialogManager;
import com.iqitech.ng.Utils.Constant;
import com.iqitech.ng.Utils.PrefUtils;
import com.iqitech.ng.agents.OtherVendingCompletionActivity;
import com.iqitech.ng.agents.PreviewActivity;
import com.iqitech.ng.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class AffordablesPreviewActivity extends AppCompatActivity {
    AlertDialogManager alertDialogManager;
    ImageView imageLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affordables_preview);
        final ActionBar abar = getSupportActionBar();
        View viewActionBar = getLayoutInflater().inflate(R.layout.custom_bar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        TextView txttitle = (TextView) viewActionBar.findViewById(R.id.txtTitle);
        txttitle.setText("V E N D I N G  I N F O R M A T I O N");
        txttitle.setTextSize(14);
        if (abar != null) {
            abar.setCustomView(viewActionBar, params);
            abar.setDisplayShowCustomEnabled(true);
            abar.setDisplayShowTitleEnabled(false);
            abar.setHomeButtonEnabled(false);
        }

        TextView vendingCode = findViewById(R.id.vendingCode);
        TextView customer_number = findViewById(R.id.customer_number);
        TextView Amount = findViewById(R.id.Amount);
        TextView vending_status = findViewById(R.id.vending_status);
        TextView customer_name = findViewById(R.id.customer_name);
        TextView service = findViewById(R.id.service);
        TextView quantity = findViewById(R.id.quantity);
        TextView deliveryaddress = findViewById(R.id.deliveryaddress);
        TextView collectionPoint = findViewById(R.id.collectionPoint);
        TextView deliveryDate = findViewById(R.id.deliveryDate);
        TextView preferredDelivery = findViewById(R.id.preferredDelivery);
        TextView deliveryFee = findViewById(R.id.deliveryfee);
        TextView totalamount = findViewById(R.id.totalamount);
        ImageView logo = findViewById(R.id.logo);

        alertDialogManager = new AlertDialogManager();

        UserModel model = PrefUtils.getCurrentUser(AffordablesPreviewActivity.this);
        ACProgressFlower dialog = new ACProgressFlower.Builder(AffordablesPreviewActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Please wait...")
                .fadeColor(Color.DKGRAY).build();
//
        Intent ii = getIntent();
        vendingCode.setText(ii.getStringExtra("vendingCode"));
        String confrim = Constant.VENDING_CONFIRM + "/" + ii.getStringExtra("vendingCode");
        Log.d("urlll", confrim);
        //Make request for JSONObject
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                Request.Method.GET, confrim, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("CONFIRM RESPONSE", response.toString());
                        dialog.dismiss();
                        try {
                            String statusCode = response.getString("statusCode");
                            JSONObject objjj = response.getJSONObject("content");
                            Log.d("stat", statusCode);
                            if (statusCode.equals("200")) {

                                Amount.setText("₦ " + objjj.getString("amount"));
                                vending_status.setText(objjj.getString("status"));
                                customer_name.setText(objjj.getString("accountName"));
                                service.setText(objjj.getString("service"));
                                quantity.setText(objjj.getString("numberOfPins"));
                                deliveryaddress.setText(objjj.getString("deliveryAddressLine2"));
                                collectionPoint.setText(objjj.getString("collectionPoint"));
                                deliveryDate.setText(objjj.getString("deliveryDate"));
                                deliveryFee.setText("₦ " + objjj.getString("deliveryFee"));
                                double total = Double.parseDouble(objjj.getString("deliveryFee")) + Double.parseDouble(objjj.getString("amount"));
                                totalamount.setText("₦ " + String.valueOf(total));

                                if (objjj.getString("deliveryOption").equals("1")) {
                                    preferredDelivery.setText("Collection Point");
                                }
                                if (objjj.getString("deliveryOption").equals("2")) {
                                    preferredDelivery.setText("Courier / Delivery");
                                }

                                String ImageURL = Constant.Base2() + "/" + objjj.getString("vendorLogo");
                                Glide
                                        .with(AffordablesPreviewActivity.this)
                                        .load(ImageURL)
                                        .centerCrop()
                                        .into(logo);


                            } else {
                                //alertDialogManager.showAlertDialog(IbadanElectricityPreviewActivity.this, "Failed", "Registration failed", false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AffordablesPreviewActivity.this, "Error occurred while vending", Toast.LENGTH_LONG).show();
                VolleyLog.d("TAGGG", "Error: " + error.getMessage());
                // As of f605da3 the following should work
                dialog.dismiss();
                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        JSONObject obj = new JSONObject(res);
                        Log.d("error", obj.getString("message"));
                        Toast.makeText(AffordablesPreviewActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();

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


//        customer_number.setText(ii.getStringExtra("customerNumber"));
//        Amount.setText(ii.getStringExtra("amount"));
//        vending_status.setText("Completed");
//        customer_name.setText(ii.getStringExtra("accountName"));
//        //  pin.setText(ii.getStringExtra("pin"));
//        service.setText(ii.getStringExtra("service"));
//        //  quantity_text.setText(ii.getStringExtra("quantity"));
//
//        String ImageURL = Constant.Base2() + "/" + ii.getStringExtra("vendorLogo");
//        Glide
//                .with(this)
//                .load(ImageURL)
//                .centerCrop()
//                .into(logo);


        Button btnComplete = findViewById(R.id.btnComplete);
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                JSONObject params = new JSONObject();
                try {
                    params.put("vendingCode", ii.getStringExtra("vendingCode"));
                    //Log.d("PARAMS", params.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Make request for JSONObject
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(
                        Request.Method.POST, Constant.VENDING_COMPLETE, params,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("RRRESPONSE555", response.toString());
                                dialog.dismiss();
                                try {
                                    String statusCode = response.getString("statusCode");
                                    // Log.d("stat", statusCode);
                                    if (statusCode.equals("200")) {
                                        alertDialogManager.showAlertDialog(AffordablesPreviewActivity.this, "Success", response.getString("message"), true);

                                        Intent iiiii = new Intent(AffordablesPreviewActivity.this, AffordablesOtherVendingCompletionActivity.class);
                                        iiiii.putExtra("vendingCode", ii.getStringExtra("vendingCode"));
                                        iiiii.putExtra("status", ii.getStringExtra("status"));
                                        iiiii.putExtra("service", ii.getStringExtra("service"));
                                        iiiii.putExtra("accountName", ii.getStringExtra("accountName"));
                                        iiiii.putExtra("type", ii.getStringExtra("type"));
                                        iiiii.putExtra("amount", ii.getStringExtra("amount"));
                                        iiiii.putExtra("customerNumber", ii.getStringExtra("customerNumber"));
                                        iiiii.putExtra("serviceId", ii.getStringExtra("serviceId"));
                                        iiiii.putExtra("vendorLogo", ii.getStringExtra("vendorLogo"));
                                        iiiii.putExtra("id", ii.getStringExtra("id"));
                                        startActivity(iiiii);
                                        finish();

                                    } else {
                                        alertDialogManager.showAlertDialog(AffordablesPreviewActivity.this, "Failed", "Registration failed", false);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("TAGGG", "Error: " + error.getMessage());
                        // As of f605da3 the following should work
                        dialog.dismiss();
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                // Now you can use any deserializer to make sense of data
                                JSONObject obj = new JSONObject(res);
                                //Log.d("error", obj.getString("message"));
                                Toast.makeText(AffordablesPreviewActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();
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
                        //Log.d("TAG", "getHeaders: " + headers.toString());
                        return headers;
                    }
                };

                DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                jsonObjReq.setRetryPolicy(retryPolicy);
                AppController.getInstance().addToRequestQueue(jsonObjReq);
            }
        });
    }
}