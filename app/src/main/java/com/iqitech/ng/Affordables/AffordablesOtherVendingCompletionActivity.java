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
import com.iqitech.ng.DashboardUtils.DashboardActivity;
import com.iqitech.ng.DealerUtils.DealerDashboardActivity;
import com.iqitech.ng.Models.UserModel;
import com.iqitech.ng.R;
import com.iqitech.ng.Utils.Constant;
import com.iqitech.ng.Utils.PrefUtils;
import com.iqitech.ng.agents.AgentDashboardActivity;
import com.iqitech.ng.agents.OtherVendingCompletionActivity;
import com.iqitech.ng.app.AppController;
import com.iqitech.ng.electricityutils.IbadanElectricityPreviewActivity;
import com.iqitech.ng.superdealerutils.SuperDealerDashboardActivity;
import com.iqitech.ng.sysadmin.SystemAdminActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;

public class AffordablesOtherVendingCompletionActivity extends AppCompatActivity {
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affordables_other_vending_completion);
        final ActionBar abar = getSupportActionBar();
        View viewActionBar = getLayoutInflater().inflate(R.layout.custom_bar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        TextView txttitle = (TextView) viewActionBar.findViewById(R.id.txtTitle);
        txttitle.setText("V E N D I N G  C O M P L E T E D");
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

        UserModel model = PrefUtils.getCurrentUser(AffordablesOtherVendingCompletionActivity.this);
        Intent ii = getIntent();
        vendingCode.setText(ii.getStringExtra("vendingCode"));

        ACProgressFlower dialog = new ACProgressFlower.Builder(AffordablesOtherVendingCompletionActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Please wait...")
                .fadeColor(Color.DKGRAY).build();

        dialog.show();
        JSONObject params2 = new JSONObject();
        try {
            params2.put("vendingCode", ii.getStringExtra("vendingCode"));
            Log.d("PARAMS", params2.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String confrim = Constant.VENDING_CONFIRM + "/" + ii.getStringExtra("vendingCode");
        // Make request for JSONObject
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
                                        .with(AffordablesOtherVendingCompletionActivity.this)
                                        .load(ImageURL)
                                        .centerCrop()
                                        .into(logo);

//


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
                Toast.makeText(AffordablesOtherVendingCompletionActivity.this, "Error occurred while vending", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(AffordablesOtherVendingCompletionActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();

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


        customer_number.setText(ii.getStringExtra("customerNumber"));
        Amount.setText(ii.getStringExtra("amount"));
        vending_status.setText("Completed");
        customer_name.setText(ii.getStringExtra("accountName"));
        //  pin.setText(ii.getStringExtra("pin"));
        service.setText(ii.getStringExtra("service"));
        //  quantity_text.setText(ii.getStringExtra("quantity"));

        String ImageURL = Constant.Base2() + "/" + ii.getStringExtra("vendorLogo");
        Glide
                .with(this)
                .load(ImageURL)
                .centerCrop()
                .into(logo);


        Button btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserModel userModel = PrefUtils.getCurrentUser(AffordablesOtherVendingCompletionActivity.this);

                switch (userModel.getRole()) {
                    case "SYSTEM ADMIN": {

                        Intent ii = new Intent(AffordablesOtherVendingCompletionActivity.this, SystemAdminActivity.class);
                        startActivity(ii);
                        break;
                    }
                    case "DEALER": {

                        Intent ii = new Intent(AffordablesOtherVendingCompletionActivity.this, DealerDashboardActivity.class);
                        startActivity(ii);
                        break;
                    }
                    case "AGENT": {

                        Intent ii = new Intent(AffordablesOtherVendingCompletionActivity.this, AgentDashboardActivity.class);
                        startActivity(ii);
                        break;
                    }
                    case "REGULAR CUSTOMER": {

                        Intent ii = new Intent(AffordablesOtherVendingCompletionActivity.this, DashboardActivity.class);
                        startActivity(ii);
                        break;
                    }
                    case "SUPER DEALER": {
                        Intent ii = new Intent(AffordablesOtherVendingCompletionActivity.this, SuperDealerDashboardActivity.class);
                        startActivity(ii);
                        break;
                    }
                }

            }
        });


    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }
}