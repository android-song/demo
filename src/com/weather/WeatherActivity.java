package com.weather;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	private TextView txCity;
	private Button btnSearch;
	private Handler weatherhandler;
	private Dialog progressDialog;
	private Timer timer;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        timer = new Timer();
        txCity = (TextView)findViewById(R.id.txCity);
        btnSearch = (Button)findViewById(R.id.btnSearch);
        progressDialog = new AlertDialog.Builder(this)
        .setTitle("读取数据中")
        .setMessage("正在加载数据，请稍等")
        .create();
        
        weatherhandler = new Handler(){
        	public void handleMessage(Message msg){
        		final String cityName = txCity.getText().toString().trim();
        		searchWeather(cityName);
        		progressDialog.hide();
        	}
        };
        
        btnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				progressDialog.show();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						Message msg = new Message();
						msg.setTarget(weatherhandler);
						msg.sendToTarget();
					}
				},100);
			}
		});
    }
    private void searchWeather(String city){
    	SAXParserFactory spf = SAXParserFactory.newInstance();
    	try {
			SAXParser sp = spf.newSAXParser();
			XMLReader reader = sp.getXMLReader();
			XmlHandler handler = new XmlHandler();
			reader.setContentHandler(handler);
			URL url = new URL("http://www.google.com/ig/api?hl=zh-cn&weather="+URLEncoder.encode(city));
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is, "GBK");
			InputSource source = new InputSource(isr);
			reader.parse(source);
			List<Weather>weatherList = handler.getWeatherList();
			TableLayout table = (TableLayout)findViewById(R.id.table);
			table.removeAllViews();
			for(Weather weather:weatherList){
				TableRow row = new TableRow(this);
				row.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
				row.setGravity(Gravity.CENTER_VERTICAL);
				ImageView img = new ImageView(this);
				img.setImageDrawable(loadImage(weather.getImageUrl()));
				img.setMinimumHeight(80);
				row.addView(img);
				TextView day = new TextView(this);
				day.setText(weather.getDay());
				day.setGravity(Gravity.CENTER_HORIZONTAL);
				row.addView(day);
				TextView temp = new TextView(this);
				temp.setText(weather.getLowTemp()+"℃-"+weather.getHighTemp()+"℃");
				temp.setGravity(Gravity.CENTER_HORIZONTAL);
				row.addView(temp);
				TextView condition = new TextView(this);
				condition.setText(weather.getCondition());
				condition.setGravity(Gravity.CENTER_HORIZONTAL);
				row.addView(condition);
				table.addView(row);
			}
		} catch (IOException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this)
				.setTitle("解析错误")
				.setMessage("获取天气数据失败，请稍候再试。")
				.setNegativeButton("确定", null)
				.show();
		} catch (SAXException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this)
			.setTitle("解析错误1")
			.setMessage("获取天气数据失败，请稍候再试。")
			.setNegativeButton("确定", null)
			.show();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			new AlertDialog.Builder(this)
			.setTitle("解析错误2")
			.setMessage("获取天气数据失败，请稍候再试。")
			.setNegativeButton("确定", null)
			.show();
		}
    	
    }
	private Drawable loadImage(String imageUrl) {
		try {
			return Drawable.createFromStream((InputStream) new URL("http://www.google.com/"+imageUrl).getContent(), "test");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}