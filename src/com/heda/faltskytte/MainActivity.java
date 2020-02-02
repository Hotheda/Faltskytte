package com.heda.faltskytte;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

	/*
	 * Objekt i XML filen
	 */
	private Button start;
	private Chronometer chrono;
	private TextView txtTime, txtReady, txtEld, txtEldUpp;
	private NumberPicker numPick;
	private ProgressBar progBar;
	
	/*
	 * Variablar
	 */
	private boolean soundOn = false;
	private boolean vibeOn = false;
	private boolean readyOn = false;
	private boolean isRunning = false;
	private int totaltime = 10;
	private int roll = 0;
	
	/*
	 * �vriga funktioner
	 */
	private Vibrator vibrate;
	private ToneGenerator tone;
	private SharedPreferences prefs;
	
	/*
	 * Statiska
	 */
	private static final String PREF_SOUND = "SOUND";
	private static final String PREF_VIBE = "VIBE";
	private static final String PREF_READY = "READY";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
         * L�gg till alla grejjer fr�n XML filen
         */
        progBar = (ProgressBar)findViewById(R.id.progressBar1);
        start = (Button)findViewById(R.id.btn_start);
        chrono = (Chronometer)findViewById(R.id.chronome);
        txtTime = (TextView)findViewById(R.id.textView);
        txtReady = (TextView)findViewById(R.id.textReady);
        txtEld = (TextView)findViewById(R.id.textEld);
        txtEldUpp = (TextView)findViewById(R.id.textEldUpp);
        numPick = (NumberPicker)findViewById(R.id.numberPicker1);
        
        //startv�rden
        txtTime.setVisibility(TextView.INVISIBLE);
        numPick.setMinValue(3);
        numPick.setMaxValue(60);
        numPick.setValue(10);
        setProgBarCountdown();
        
        /*
         * L�s in sparade alternativ (ljud vibration)
         * l�s in fr�n sharedprefereces och spara i variabel
         */
        prefs = getSharedPreferences("com.heda.faltskytte",MODE_PRIVATE);
        soundOn = prefs.getBoolean(PREF_SOUND, true);
        vibeOn = prefs.getBoolean(PREF_VIBE, true);
        readyOn = prefs.getBoolean(PREF_READY, true);
        
        vibrate = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        
        /*
         * S�tt timern som 0 p� aktuell tid och starta nedr�kningen
         * finishtime �r aktuell tid + 10 sek.
         */
        start.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(isRunning){
					isRunning=false;
					resettimer();
				}else{
					isRunning = true;
					roll = -11;
					totaltime = numPick.getValue();
					chrono.setBase(SystemClock.elapsedRealtime());				
					chrono.start();
					
					//Visa textrutan och ta bort tid pickern
					txtTime.setVisibility(TextView.VISIBLE);
					numPick.setVisibility(NumberPicker.INVISIBLE);
					
					//S�tt in tider i olika textf�lten
					txtEldUpp.setText("Eld..upp..h�r (" + String.valueOf(totaltime-3) + " - "+totaltime+")");
					
					//visa textrutorna
					txtReady.setVisibility(TextView.VISIBLE);
					txtEld.setVisibility(TextView.VISIBLE);
					txtEldUpp.setVisibility(TextView.VISIBLE);
					
					/*
					 * ProgressBar
					 */
					setProgBarCountdown();
				}
			}
		});
        
        /*
         * R�kna varje g�ng chronometern tickar
         */
        chrono.setOnChronometerTickListener(new OnChronometerTickListener(){
			@Override
			public void onChronometerTick(Chronometer arg0) {
				if(roll > totaltime){
					//chrono.stop();
					start.setText("Patron ur, visitation.");
					//�terst�ll startknappen
					start.setEnabled(true);
				}else{	
					//Visa text p� knapp beroende p� tidpunkt
					if(roll==-10){
						start.setText("10 sekunder kvar");
						start.setEnabled(false);
					}
					else if(roll==-3){
						start.setText("F�rdiga");
						txtReady.setTextColor(Color.RED);
						if(readyOn){
							if(vibeOn)
								vibrate.vibrate(100);
							if(soundOn)
								beep(100);
						}
					}
					else if(roll==0){
						//St�ll om ProgressBar till uppr�kning
						setProgBarFire();						
						start.setText("E L D !");
						txtEld.setTextColor(Color.RED);
						if(vibeOn)
							vibrate.vibrate(300);
						if(soundOn)
							beep(300);
					}
					else if(roll==totaltime-3){
						start.setText("..Eld..");
						txtEldUpp.setTextColor(Color.RED);
						if(vibeOn)
							vibrate.vibrate(200);
						if(soundOn)
							beep(100);
					}
					else if(roll==totaltime-2){
						start.setText("..upp..");
						if(vibeOn)
							vibrate.vibrate(100);
						if(soundOn)
							beep(100);
					}
					else if(roll==totaltime-1){
						start.setText("..h�r.");
						if(vibeOn)
							vibrate.vibrate(100);
						if(soundOn)
							beep(100);
					}else if(roll==totaltime){
						//Slut p� tiden
						txtTime.setTextColor(Color.RED);
					}
					
					//Uppr�kning eller nedr�kning?
					if(roll<0){
						progBar.setSecondaryProgress(Math.abs(roll));
						if(Math.abs(roll)<3)
							progBar.setProgress(Math.abs(roll));
					}else{					
						//Progressbar progress
						progBar.setProgress(roll);
					}
					
					//visa tid i textbox och rulla en sekund
					txtTime.setText(String.valueOf(roll));
					roll++;
				}
			}
        });
    }
    
    private void resettimer(){
		/* 
		 * tillbaks med standardtexten
		 */
    	chrono.stop();
		start.setText("10 sekunder kvar");
		numPick.setVisibility(NumberPicker.VISIBLE);
		txtTime.setVisibility(TextView.INVISIBLE);
		//�terst�ll f�rg i textrutor
		txtReady.setTextColor(Color.BLACK);
		txtEld.setTextColor(Color.BLACK);
		txtEldUpp.setTextColor(Color.BLACK);
		txtTime.setTextColor(Color.BLACK);
		//d�lj textrutorna
		txtReady.setVisibility(TextView.INVISIBLE);
		txtEld.setVisibility(TextView.INVISIBLE);
		txtEldUpp.setVisibility(TextView.INVISIBLE);
		
    }

    private void setProgBarFire(){
    	progBar.setMax(totaltime);
    	progBar.setSecondaryProgress(totaltime-3);
    	//nolla progressbar
		progBar.setProgress(0);
    }
    
    private void setProgBarCountdown(){
    	progBar.setMax(10);
    	progBar.setProgress(3);
    	//nolla progressbar
		progBar.setSecondaryProgress(10);
    }
    
    /*
     * ToneGenerator f�r att f� fram ett Beep
     * ( typ av ljud, l�ngd p� ljud )
     */
    private void beep(int millies){
    	tone.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, millies);
    }
    
    
    /*
     * Options menu, st�nga av/p� ljud och vibration
     * L�gg till vibration och ljud med check ruta
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	/*
    	 * L�gg till menyval
    	 */
    	menu.add(1,1,1,"Ljud").setCheckable(true);
    	menu.add(1,2,2,"Vibration").setCheckable(true);
    	menu.add(1,3,3,"Vid f�rdiga").setCheckable(true);
    	
    	/*
    	 * Checka i om dessa �r p�/av
    	 */
    	menu.getItem(0).setChecked(soundOn);
    	menu.getItem(1).setChecked(vibeOn);
    	menu.getItem(2).setChecked(readyOn);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	case 1:
    		soundOnOff(item);
    		break;
    	case 2:
    		vibeOnOff(item);
    		break;
    	case 3:
    		whenReadyOnOff(item);
    		break;
    	}
		return true;
    }
    
    private void whenReadyOnOff(MenuItem item) {
    	/*
    	 * St�ng av/p� samt kryssa av/i rutan
    	 */
    	if(item.isChecked()){
    		item.setChecked(false);
    		readyOn=false;
    	}else{
    		item.setChecked(true);
    		readyOn=true;
    	}
    	prefs.edit().putBoolean(PREF_READY, readyOn).commit();
	}

	/*
     * St�ng av / s�tt p� ljud
     */
    private void soundOnOff(MenuItem item){
    	/*
    	 * St�ng av/p� samt kryssa av/i rutan
    	 */
    	if(item.isChecked()){
    		item.setChecked(false);
    		soundOn=false;
    	}else{
    		item.setChecked(true);
    		soundOn=true;
    	}
    	prefs.edit().putBoolean(PREF_SOUND, soundOn).commit();
    }
    
    
    /*
     * St�ng av / s�tt p� vibration
     */
    private void vibeOnOff(MenuItem item){
    	/*
    	 * St�ng av/p� samt kryssa av/i rutan
    	 */
    	if(item.isChecked()){
    		item.setChecked(false);
    		vibeOn=false;
    	}else{
    		item.setChecked(true);
    		vibeOn=true;
    	}
    	prefs.edit().putBoolean(PREF_VIBE, vibeOn).commit();
    }
    
}
