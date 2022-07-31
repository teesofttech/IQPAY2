package com.iqitech.ng.Affordables;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.iqitech.ng.Adapter.TransactionListAdapter;
import com.iqitech.ng.BalanceUtils.TestTableActivity;
import com.iqitech.ng.Models.UserModel;
import com.iqitech.ng.Models.VendingLogs;
import com.iqitech.ng.R;
import com.iqitech.ng.Utils.Constant;
import com.iqitech.ng.Utils.PrefUtils;
import com.iqitech.ng.app.AppController;
import com.iqitech.ng.reports.FundingTableActivity;
import com.iqitech.ng.sysadmin.ReprintActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import ir.androidexception.datatable.DataTable;
import ir.androidexception.datatable.model.DataTableHeader;
import ir.androidexception.datatable.model.DataTableRow;

public class AffordablesReportActivity extends AppCompatActivity {
    private ArrayList<VendingLogs> cartList;
    private TransactionListAdapter mAdapter;
    String CategoryId;
    ProgressDialog progressDialog;
    UserModel model;
    ACProgressFlower dialog;
    private SearchView searchView;

    ImageView imageView;
    TextView go_to_homepage;
    ArrayList<DataTableRow> rows;
    // @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    // @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    DataTable dataTable;
    DataTableHeader header;
    EditText fromDate, toDate;
    private int mYear, mMonth, mDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affordables_report);

        final ActionBar abar = getSupportActionBar();
        View viewActionBar = getLayoutInflater().inflate(R.layout.custom_bar, null);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        TextView txttitle = (TextView) viewActionBar.findViewById(R.id.txtTitle);
        txttitle.setText("AFFORDABLE FARM REPORTS");
        txttitle.setTextSize(14);
        if (abar != null) {
            abar.setCustomView(viewActionBar, params);

            abar.setDisplayShowCustomEnabled(true);
            abar.setDisplayShowTitleEnabled(false);
            abar.setHomeButtonEnabled(false);
        }
        model = PrefUtils.getCurrentUser(AffordablesReportActivity.this);

        imageView = findViewById(R.id.imageView);
        go_to_homepage = findViewById(R.id.go_to_homepage);
        //  mAdapter = new TransactionListAdapter(this, cartList, TestTableActivity.this);
        mRecyclerView = findViewById(R.id.recycle_view);
        cartList = new ArrayList<>();
        dialog = new ACProgressFlower.Builder(AffordablesReportActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Please wait...")
                .fadeColor(Color.DKGRAY).build();
        fromDate = findViewById(R.id.fromDate);

        toDate = findViewById(R.id.toDate);

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH) + 1;
                mDay = calendar.get(Calendar.DAY_OF_MONTH);

                //show dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(AffordablesReportActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fromDate.setText((dayOfMonth < 10 ? ("0" + dayOfMonth) : (dayOfMonth)) + "-" + (mMonth < 10 ? ("0" + mMonth) : (mMonth)) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });


        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH) + 1;
                mDay = calendar.get(Calendar.DAY_OF_MONTH);

                //show dialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(AffordablesReportActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        toDate.setText((dayOfMonth < 10 ? ("0" + dayOfMonth) : (dayOfMonth))  + "-" + (mMonth < 10 ? ("0" + mMonth) : (mMonth)) + "-" + year);
                    }
                }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
        // define 200 fake rows for table

        Button btn_click_print = (Button) findViewById(R.id.btn_click_print);
        btn_click_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AffordablesReportActivity.this, ReprintActivity.class));
            }
        });


        Button btnsearch = findViewById(R.id.btnsearch);
        btnsearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.show();
                ArrayList<VendingLogs> records = getRecords(fromDate, toDate);

                dataTable = findViewById(R.id.data_table);

                header = new DataTableHeader.Builder()
                        .item("Date", 1)
                        .item("V. Code", 1)
                        .item("Service", 1)
                        .item("W. Balance", 1)
                        .item("Amount", 1)
                        .item("Status", 1)
                        .build();

                rows = new ArrayList<>();

            }
        });

    }

    public ArrayList<VendingLogs> getRecords(EditText fromDate, EditText toDate) {
        String newUrl = "";
        if (fromDate.getText().toString().equals("") && toDate.getText().toString().equals("")) {
            newUrl = Constant.AFFORDABLESREPORT;
        } else {
            newUrl = Constant.AFFORDABLESREPORT + "?startDate=" + fromDate.getText().toString() + "&endDate=" + toDate.getText().toString();
        }
        Log.d("URL", newUrl);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                newUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("RESPONSE", response.toString());
                dialog.dismiss();
                try {

                    if (response.getString("message").equals("")) {
                        imageView.setVisibility(View.VISIBLE);
                        go_to_homepage.setVisibility(View.VISIBLE);
                        mRecyclerView.setVisibility(View.GONE);
                    } else {

                        JSONArray content = response.getJSONArray("content");
                        //categories

                        if (content != null && content.length() > 0) {
                            for (int i = 0; i < content.length(); i++) {
                                JSONObject vend = (JSONObject) content.get(i);

                                VendingLogs category = new VendingLogs();
                                category.setService(vend.getString("service"));
                                category.setAmount(vend.getString("amount"));
                                category.setCustomerNumber(vend.getString("customerNumber"));
                                category.setTransactionDate(vend.getString("transactionDate"));
                                category.setVendingCode(vend.getString("vendingCode"));
                                category.setStatus(vend.getString("status"));
                                category.setWalletBalance(vend.getString("walletBalance"));
                                cartList.add(category);
                                //mAdapter.notifyDataSetChanged();
                            }
                            for (int i = 0; i < cartList.size(); i++) {
                                VendingLogs logs = (VendingLogs) cartList.get(i);
                                Log.d("records", logs.getAmount());

                                DataTableRow row = new DataTableRow.Builder()
                                        .value(logs.getTransactionDate())
                                        .value(logs.getVendingCode())
                                        .value(logs.getService())
                                        .value(logs.getWalletBalance())
                                        .value(logs.getAmount())
                                        .value(logs.getStatus())
                                        .build();


                                rows.add(row);
                            }

                            //  dataTable.setTypeface(typeface);
                            dataTable.setHeader(header);
                            dataTable.setRows(rows);
                            dataTable.inflate(getApplicationContext());
                            Log.d("size", String.valueOf(cartList.size()));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
                //progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(AffordablesReportActivity.this, "Error occurred while vending", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(AffordablesReportActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();

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

        return cartList;
    }
}