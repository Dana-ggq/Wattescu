package com.example.wattescu.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.Settings;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.AllianceLoader;
import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.CustomConfigurationActivity;
import com.example.wattescu.R;
import com.example.wattescu.async.AsyncTaskRunner;
import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseConsumptionService;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.python.api.prediction.PredictionApi;
import com.example.wattescu.python.api.prediction.PredictionRequestObject;
import com.example.wattescu.util.CurrentConfigurationConsumption;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Bulb;
import com.example.wattescu.entities.Provider;
import com.example.wattescu.entities.Room;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GraphicsFragment extends Fragment {

    public static final String CAMERE = "Camere";
    public static final String NUME = "Nume";
    public static final String TIP = "Tip";
    public static final String NUMAR_BECURI = "Numar becuri";
    public static final String NUMAR_ELECTROCASNICE = "Numar electrocasnice";
    public static final String CONSUM_MEDIU_KWH = "Consum mediu(KWh)";
    public static final String CHELTUIELI_MEDII_LEI = "Cheltuieli medii(lei)";
    public static final String CLASA_ENERGETICA = "Clasa energetica";
    public static final String ELECTROCASNICE = "Electrocasnice";
    public static final String BECURI = "Becuri";
    private static final String TAG = "permission_write";
    private List<Bulb> bulbs;
    private List<Appliance> appliances;
    private List<Room> rooms;
    private Double priceKw = 0.0;

    //firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;
    String currentUserid;

    //views
    private FloatingActionButton fabOpenSave;
    private FloatingActionButton fabDownload;
    private FloatingActionButton fabSendEmail;
    private Spinner spnGraphicType;
    private CircularDotsLoader progressBar;
    private TextView tvThisMonthCo2Emissions;
    private TextView tvThisMonthConsumption;
    private TextView getTvThisMonthSpendings;
    private  TextView tvTitle;
    private TextView tvPredict;
    private TextView tvPredictValue;
    private ConstraintLayout clPrediction;
    private ConstraintLayout clParent;
    private AllianceLoader pbPrediction;
    
    //fab animation
    private Animation animFabOpen, animFabClose;
    private boolean isFabOpen = false;

    //chart
    private PieChart pieChart;

    //excel
    private String filename;


    public GraphicsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graphics, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        pieChart = view.findViewById(R.id.fragment_graphics_pie_chart);
        tvTitle = view.findViewById(R.id.fragment_graphics_tv_month_year);
        tvThisMonthConsumption = view.findViewById(R.id.fragment_graphics_tv_month_year);
        fabOpenSave = view.findViewById(R.id.fragment_graphics_fab_save_file);
        fabDownload = view.findViewById(R.id.fragment_graphics_fab_download);
        fabSendEmail = view.findViewById(R.id.fragment_graphics_fab_send_email);
        spnGraphicType = view.findViewById(R.id.fragment_graphics_spn_graph_type);
        progressBar = view.findViewById(R.id.fragment_graphics_progress_bar);
        tvThisMonthCo2Emissions = view.findViewById(R.id.fragment_graphics_tv_this_month_CO2);
        tvThisMonthConsumption = view.findViewById(R.id.fragment_graphics_tv_this_month_consumption);
        getTvThisMonthSpendings = view.findViewById(R.id.fragment_graphics_tv_this_month_spendings);
        tvPredict = view.findViewById(R.id.fragment_graphics_tv_predict);;
        tvPredictValue = view.findViewById(R.id.fragment_graphics_tv_prediction_result);
        clPrediction = view.findViewById(R.id.fragment_graphics_cl_prediction);
        clParent = view.findViewById(R.id.fragment_graphics_cl);
        pbPrediction = view.findViewById(R.id.fragment_graphics_pb_predict);
        if(getContext()!=null){
            appliances = new ArrayList<>();
            bulbs = new ArrayList<>();
            rooms = new ArrayList<>();

            initTitle();
            setupPieChart();
            initFirebaseOperations();
            setSpnGrapficTypeValues();
            initAnimations();
            initEvents();
        }
    }


    @SuppressLint("NewApi")
    private void initTitle() {
        LocalDate currentDate = LocalDate.now();
        Month m = currentDate.getMonth();
        String month="";
        int y = currentDate.getYear();
        switch (m){
            case JANUARY:
                month = "IAN";
                break;
            case FEBRUARY:
                month = "FEB";
                break;
            case MARCH:
                month = "MAR";
                break;
            case APRIL:
                month = "APR";
                break;
            case MAY:
                month = "MAI";
                break;
            case JULY:
                month = "IUL";
                break;
            case JUNE:
                month = "IUN";
                break;
            case AUGUST:
                month = "AUG";
                break;
            case SEPTEMBER:
                month = "SEP";
                break;
            case OCTOBER:
                month = "OCT";
                break;
            case NOVEMBER:
                month = "NOV";
                break;
            case DECEMBER:
                month = "DEC";
                break;
        }
        tvTitle.setText(getString(R.string.format_date_graph,month,y));
        filename = getString(R.string.format_excel_title,month,y);
    }

    private void initFirebaseOperations() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            //get current user id
            firebaseAuth = FirebaseAuth.getInstance();
            currentUserid = firebaseAuth.getCurrentUser().getUid();
            //init firebase configuration service
            firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(currentUserid);
            firebaseCurrentConfigurationService.getRoomsDataChangeListener(getRoomsListener());
            firebaseCurrentConfigurationService.getProvider(getProviderCallback());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Callback<Provider> getProviderCallback() {
        return new Callback<Provider>() {
            @Override
            public void runResultOnUiThread(Provider result) {
                progressBar.setVisibility(View.VISIBLE);
                if(result!=null){
                    priceKw = result.getPriceKw();
                }
                firebaseCurrentConfigurationService.getAppliancesDataChangeListener(getAppliancerCallback());
            }
        };
    }
    private Callback<List<Appliance>> getAppliancerCallback() {
        return new Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if(result!=null){
                    appliances.clear();
                    appliances.addAll(result);
                    firebaseCurrentConfigurationService.getBulbsDataChangeListener(getBulbsCallback());
                }
            }
        };
    }

    private Callback<List<Bulb>> getBulbsCallback() {
        return new Callback<List<Bulb>>() {
            @Override
            public void runResultOnUiThread(List<Bulb> result) {
                if(result!=null){
                    bulbs.clear();
                    bulbs.addAll(result);
                    initConsumptionTextViews();
                }
                progressBar.setVisibility(View.GONE);
                spnGraphicType.setSelection(1);
            }
        };
    }

    private void initConsumptionTextViews() {
        tvThisMonthConsumption.setText(String.valueOf(Math.round(CurrentConfigurationConsumption.getMonthlyConsumption(bulbs,appliances))+1));
        tvThisMonthCo2Emissions.setText(String.valueOf(Math.round(CurrentConfigurationConsumption.getMonthlyCo2Emissions(bulbs,appliances))+1));
        getTvThisMonthSpendings.setText(String.valueOf(Math.round(CurrentConfigurationConsumption.getMonthlySpendings(bulbs,appliances,priceKw))));
    }

    private Callback<List<Room>> getRoomsListener() {
        return new Callback<List<Room>>() {
            @Override
            public void runResultOnUiThread(List<Room> result) {
                if(result!=null){
                    rooms.clear();
                    rooms.addAll(result);
                }
            }
        };
    }

    private void setSpnGrapficTypeValues() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),R.array.graphic_type, android.R.layout.simple_spinner_dropdown_item);
        spnGraphicType.setAdapter(adapter);
    }

    private void initAnimations() {
        animFabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        animFabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
    }

    private void initEvents() {
        fabOpenSave.setOnClickListener(getFabOpenSaveListener());
        fabDownload.setOnClickListener(getFabDownloadExcelListener());
        fabSendEmail.setOnClickListener(getFabEmailListener());
        spnGraphicType.setOnItemSelectedListener(getSpinnerChartTypeSelectedListener());
        tvPredict.setOnClickListener(getPredictListener());
    }

    private View.OnClickListener getPredictListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(false);
                pbPrediction.setVisibility(View.VISIBLE);
                getPythonApiResponse();
            }
        };
    }

    private void toggle(boolean show) {
        View calcReductionsLayout = clPrediction;
        ViewGroup parent = clParent;

        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(600);
        transition.addTarget(R.id.fragment_graphics_cl_prediction);

        TransitionManager.beginDelayedTransition(parent, transition);
        calcReductionsLayout.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void getPythonApiResponse() {

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .callTimeout(4, TimeUnit.MINUTES)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        //create retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://danaggeorgescu.pythonanywhere.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        //create api
        PredictionApi predictionApi = retrofit.create(PredictionApi.class);
        Call<PredictionRequestObject> callApi = predictionApi.getResult(currentUserid);
        callApi.enqueue(new retrofit2.Callback<PredictionRequestObject>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<PredictionRequestObject> call, Response<PredictionRequestObject> response) {
                pbPrediction.setVisibility(View.GONE);

                if(!response.isSuccessful()){
                    //error
                    toggle(true);
                    return;
                }

                //got answer
                PredictionRequestObject predictionRequestObject = response.body();
                if(predictionRequestObject.getResult().equals(CustomConfigurationActivity.INTERNAL_ERROR)){
                    toggle(true);
                    return;
                } else{
                    tvPredictValue.setText(getContext().getString(R.string.format_consumption, Double.valueOf(predictionRequestObject.getResult())));
                    toggle(true);
                }
            }

            @Override
            public void onFailure(Call<PredictionRequestObject> call, Throwable t) {
                pbPrediction.setVisibility(View.GONE);
                toggle(true);
                return;
            }
        });


    }


    private View.OnClickListener getFabEmailListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password  = "zpcqokdaornktnck";
                String fromEmail = "dhm_hory@yahoo.com";
                String toEmail = firebaseAuth.getCurrentUser().getEmail();

                Properties properties = new Properties();
                properties.put("mail.smtp.auth","true");
                properties.put("mail.smtp.starttls.enable","true");
                properties.put("mail.smtp.host", "smtp.mail.yahoo.com");
                properties.put("mail.smtp.port", "587");

                Session session = Session.getInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });

                //start mail message
                Message msg = new MimeMessage(session);
                try {
                    createMessage(fromEmail, toEmail, msg);

                    //create progress dialog
                    ProgressDialog progressDialog =
                            ProgressDialog.show(getContext(), getString(R.string.msg_wait), getString(R.string.send_email_msg), true, false);

                    AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
                    Callable<String> asyncOperationSendMail = getAsyncOperationSendMail(msg);
                    Callback<String> mailCallback = getMailCallback(progressDialog);
                    asyncTaskRunner.executeAsync(asyncOperationSendMail, mailCallback);

                }catch (Exception ex){
                    ex.printStackTrace();
                }


            }
        };
    }

    @NonNull
    private Callable<String> getAsyncOperationSendMail(Message msg) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    Transport.send(msg);
                    return getString(R.string.email_succes);
                } catch (Exception ex) {
                    return getString(R.string.login_dialog_reset_password_error);
                }
            }
        };
    }

    private void createMessage(String fromEmail, String toEmail, Message msg) throws MessagingException, IOException {
        msg.setFrom(new InternetAddress(fromEmail));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject(getString(R.string.mail_subject));

        Multipart emailContent = new MimeMultipart();

        //text part
        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(getString(R.string.mail_body_text));
        //xls part
        createXlsFile();
        MimeBodyPart xslAttachment = new MimeBodyPart();
        xslAttachment.attachFile(new File(Environment.getExternalStorageDirectory()+"/"+filename));

        emailContent.addBodyPart(textBodyPart);
        emailContent.addBodyPart(xslAttachment);

        msg.setContent(emailContent);
    }

    private Callback<String> getMailCallback(ProgressDialog progressDialog) {
        return new Callback<String>() {
            @Override
            public void runResultOnUiThread(String result) {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(),result,Toast.LENGTH_SHORT).show();
            }
        };
    }

    private View.OnClickListener getFabDownloadExcelListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createXlsFile();
            }
        };
    }

    private void createXlsFile() {
        requestReadWritePermission();
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        createRoomsSheet(hssfWorkbook);
        createAppliancesSheet(hssfWorkbook);
        createBulbsSheet(hssfWorkbook);

        File filePath = new File(Environment.getExternalStorageDirectory()+"/"+filename);
//        if(filePath.exists()){
//            return;
//        }
        writeToFile(hssfWorkbook, filePath);
    }

    private void writeToFile(HSSFWorkbook hssfWorkbook, File filePath) {
        FileOutputStream fileOutputStream = null;
        try {
            filePath.createNewFile();
            fileOutputStream = new FileOutputStream(filePath);
            hssfWorkbook.write(fileOutputStream);
            Toast.makeText(getContext(), R.string.file_saved, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(),R.string.login_dialog_reset_password_error, Toast.LENGTH_SHORT).show();
        } finally {
            if(fileOutputStream!=null){
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }



    private void createBulbsSheet(HSSFWorkbook hssfWorkbook) {
        HSSFSheet hssfSheet = hssfWorkbook.createSheet(BECURI);
        createBulbsHeader(hssfSheet);
        for (int i=0; i<bulbs.size(); i++) {
            HSSFRow row = hssfSheet.createRow(i+1);

            HSSFCell cellName = row.createCell(0);
            cellName.setCellValue(bulbs.get(i).getName());

            HSSFCell cellAvgConsumption = row.createCell(1);
            cellAvgConsumption.setCellValue(bulbs.get(i).getAverageMonthlyConsumtion());

            HSSFCell cellAvgSpendings = row.createCell(2);
            cellAvgSpendings.setCellValue(bulbs.get(i).getAverageMonthlySpendings(priceKw));

        }
    }

    private void createBulbsHeader(HSSFSheet hssfSheet) {
        HSSFRow row = hssfSheet.createRow(0);

        HSSFCell cellName = row.createCell(0);
        cellName.setCellValue(NUME);

        HSSFCell cellAvgConsumption = row.createCell(1);
        cellAvgConsumption.setCellValue(CONSUM_MEDIU_KWH);

        HSSFCell cellAvgSpendings = row.createCell(2);
        cellAvgSpendings.setCellValue(CHELTUIELI_MEDII_LEI);
    }

    private void createAppliancesSheet(HSSFWorkbook hssfWorkbook) {
        HSSFSheet hssfSheet = hssfWorkbook.createSheet(ELECTROCASNICE);
        createAppliancesHeader(hssfSheet);
        for (int i=0; i<appliances.size(); i++) {
            HSSFRow row = hssfSheet.createRow(i+1);

            HSSFCell cellName = row.createCell(0);
            cellName.setCellValue(appliances.get(i).getName());

            HSSFCell cellEfficiencyClass = row.createCell(1);
            cellEfficiencyClass.setCellValue(appliances.get(i).getEfficiencyClass());

            HSSFCell cellAvgConsumption = row.createCell(2);
            cellAvgConsumption.setCellValue(appliances.get(i).getAverageMonthlyConsumption());

            HSSFCell cellAvgSpendings = row.createCell(3);
            cellAvgSpendings.setCellValue(appliances.get(i).getAverageMonthlySpendings(priceKw));

        }
    }

    private void createAppliancesHeader(HSSFSheet hssfSheet) {
        HSSFRow row = hssfSheet.createRow(0);

        HSSFCell cellName = row.createCell(0);
        cellName.setCellValue(NUME);

        HSSFCell cellEfficiencyClass = row.createCell(1);
        cellEfficiencyClass.setCellValue(CLASA_ENERGETICA);

        HSSFCell cellAvgConsumption = row.createCell(2);
        cellAvgConsumption.setCellValue(CONSUM_MEDIU_KWH);

        HSSFCell cellAvgSpendings = row.createCell(3);
        cellAvgSpendings.setCellValue(CHELTUIELI_MEDII_LEI);
    }

    private void createRoomsSheet(HSSFWorkbook hssfWorkbook) {
        HSSFSheet hssfSheet = hssfWorkbook.createSheet(CAMERE);
        createRoomsHeader(hssfSheet);
        for (int i=0; i<rooms.size(); i++) {
            HSSFRow row = hssfSheet.createRow(i+1);

            HSSFCell cellName = row.createCell(0);
            cellName.setCellValue(rooms.get(i).getName());

            HSSFCell cellType = row.createCell(1);
            cellType.setCellValue(rooms.get(i).getType());

            HSSFCell cellNoBulbs = row.createCell(2);
            cellNoBulbs.setCellValue(rooms.get(i).getNoBulbs(bulbs));

            HSSFCell cellNoAppliances= row.createCell(3);
            cellNoAppliances.setCellValue(rooms.get(i).getNoAppliances(appliances));

            HSSFCell cellAvgConsumption = row.createCell(4);
            cellAvgConsumption.setCellValue(rooms.get(i).getAverageMonthlyConsumption(bulbs,appliances));

            HSSFCell cellAvgSpendings = row.createCell(5);
            cellAvgSpendings.setCellValue(rooms.get(i).getAverageMonthlySpendings(bulbs,appliances,priceKw));

        }
    }

    private void createRoomsHeader(HSSFSheet hssfSheet) {
        HSSFRow rowHeader = hssfSheet.createRow(0);
        HSSFCell cellNameHeader = rowHeader.createCell(0);
        cellNameHeader.setCellValue(NUME);

        HSSFCell cellTypeHeader = rowHeader.createCell(1);
        cellTypeHeader.setCellValue(TIP);

        HSSFCell cellNoBulbsHeader = rowHeader.createCell(2);
        cellNoBulbsHeader.setCellValue(NUMAR_BECURI);

        HSSFCell cellNoAppliancesHeader= rowHeader.createCell(3);
        cellNoAppliancesHeader.setCellValue(NUMAR_ELECTROCASNICE);

        HSSFCell cellAvgConsumptionHeader = rowHeader.createCell(4);
        cellAvgConsumptionHeader.setCellValue(CONSUM_MEDIU_KWH);

        HSSFCell cellAvgSpendingsHeader = rowHeader.createCell(5);
        cellAvgSpendingsHeader.setCellValue(CHELTUIELI_MEDII_LEI);
    }

    private AdapterView.OnItemSelectedListener getSpinnerChartTypeSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(bulbs!=null && rooms!=null && appliances!=null){
                    ArrayList<PieEntry> entries = new ArrayList<>();
                    float sum = 0;
                    switch (position){
                        case 0:
                            for (Room room:
                                    rooms) {
                                sum+=room.getAverageMonthlyConsumption(bulbs,appliances);
                            }
                            for (Room room:
                                 rooms) {
                                float percent = (float) (room.getAverageMonthlyConsumption(bulbs,appliances)/sum);
                                String label = room.getName();
                                if(percent!=0) {
                                    entries.add(new PieEntry(percent,label));
                                }
                            }
                            break;
                        case 1:
                            for (Bulb bulb:
                                    bulbs) {
                                sum+=bulb.getAverageMonthlyConsumtion();
                            }
                            for (Bulb bulb:
                                    bulbs) {
                                float percent = (float) (bulb.getAverageMonthlyConsumtion()/sum);
                                String label = bulb.getName();
                                if(percent!=0) {
                                    entries.add(new PieEntry(percent,label));
                                }
                            }
                            break;
                        case 2:
                            for (Appliance appliance:
                                    appliances) {
                                sum+=appliance.getAverageMonthlyConsumption();
                            }
                            for (Appliance appliance:
                                    appliances) {
                                float percent = (float) (appliance.getAverageMonthlyConsumption()/sum);
                                String label = appliance.getName();
                                if(percent!=0) {
                                    entries.add(new PieEntry(percent,label));
                                }
                            }
                            break;
                    }
                    loadPieChart(entries);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        };
    }

    private void setupPieChart(){
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(20);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

    }

    private void loadPieChart(ArrayList<PieEntry> entries) {
        ArrayList<Integer> colors = new ArrayList<>();
        initColors(colors, entries.size());

        PieDataSet dataSet = new PieDataSet(entries,"Consumption");
        dataSet.setColors(colors);

        PieData pieData = new PieData(dataSet);
        pieData.setDrawValues(true);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextSize(12f);
        pieData.setValueTextColor(Color.BLACK);

        pieChart.setData(pieData);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EaseInOutCirc);

    }

    private void initColors(ArrayList<Integer> colors, int size) {
        Random random = new Random();
        for(int i=0; i<size; i++) {
            int color = Color.argb(100,
                    random.nextInt(254) + 1,
                    random.nextInt(254) + 1,
                    random.nextInt(254) + 1);
            colors.add(color);
        }
    }

    private View.OnClickListener getFabOpenSaveListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAnimations();
            }
        };
    }

    private void requestReadWritePermission() {
        if (Build.VERSION.SDK_INT >= 30){
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                PackageManager.PERMISSION_GRANTED);

    }

    private void setAnimations(){
        if(isFabOpen){
            fabSendEmail.startAnimation(animFabClose);
            fabSendEmail.setClickable(false);
            fabDownload.startAnimation(animFabClose);
            fabDownload.setClickable(false);
            isFabOpen = false;

        }else{
            fabSendEmail.startAnimation(animFabOpen);
            fabSendEmail.setClickable(true);
            fabDownload.startAnimation(animFabOpen);
            fabDownload.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getMonthlyConsumptionData();
    }


    private void getMonthlyConsumptionData() {
        LocalDate localDate = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            localDate = LocalDate.now();
            int day = localDate.getDayOfMonth();
            if(day == 28){
                try {
                    FirebaseConsumptionService firebaseConsumptionService =  FirebaseConsumptionService.getInstance(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    firebaseConsumptionService.insertConsumptionDetails(localDate.toString(), CurrentConfigurationConsumption.getMonthlyConsumption(bulbs,appliances));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

    }
}