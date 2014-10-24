package com.example.simpleencryptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	EditText textKey, plainText, fileKey;
	Button textEncrypt, textDecrypt;
	private String filePath;
	private static final int FILE_SELECT_ENCRYPT=0;
	private static final int FILE_SELECT_DECRYPT=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TabHost tabhost=(TabHost) findViewById(android.R.id.tabhost);
		tabhost.setup();
		
		TabHost.TabSpec tabspec;
		tabspec=tabhost.newTabSpec("text");
		tabspec.setIndicator("Files");
		tabspec.setContent(R.id.tab2);
		tabhost.addTab(tabspec);
		
		tabspec=tabhost.newTabSpec("file");
		tabspec.setIndicator("Text");
		tabspec.setContent(R.id.tab1);
		tabhost.addTab(tabspec);
		
		textKey=(EditText)findViewById(R.id.textKey);
		plainText=(EditText)findViewById(R.id.plainText);
		fileKey=(EditText)findViewById(R.id.fileKey);
		
		textEncrypt=(Button)findViewById(R.id.textEncrypt);
		textDecrypt=(Button)findViewById(R.id.textDecrypt);
	}
	
	public void onClick(View v)
	{
		switch (v.getId()) {
		case R.id.textEncrypt:
			try {				
				Encrypt();
			} catch (InvalidKeyException | NoSuchAlgorithmException
					| InvalidKeySpecException | NoSuchPaddingException
					| InvalidParameterSpecException | IllegalBlockSizeException
					| BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.textDecrypt:
			try {				
				Decrypt();
			} catch (InvalidKeyException | NoSuchAlgorithmException
					| InvalidKeySpecException | NoSuchPaddingException
					| InvalidAlgorithmParameterException
					| IllegalBlockSizeException | BadPaddingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.fileEncrypt:
			ChooseFile(FILE_SELECT_ENCRYPT);			
			break;
		case R.id.fileDecrypt:
			ChooseFile(FILE_SELECT_DECRYPT);			
			break;
		default:
			break;
		}
	}
	
	private void ChooseFile(int FILE_SELECT_CODE)
	{
		Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		
		// special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        sIntent.putExtra("CONTENT_TYPE", "*/*"); 
        sIntent.addCategory(Intent.CATEGORY_DEFAULT);

        Intent chooserIntent;
        if (getPackageManager().resolveActivity(sIntent, 0) != null){
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        }
        else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }
		
		try
		{
			startActivityForResult(Intent.createChooser(chooserIntent, "Select file"), FILE_SELECT_CODE);			
		}
		catch(android.content.ActivityNotFoundException e)
		{
			Toast.makeText(this, "Please install file manager.", Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultcode, Intent data)
	{
		switch (requestCode) {
		case FILE_SELECT_ENCRYPT:
			if(resultcode==RESULT_OK)
			{
				Uri uri=data.getData();
				filePath=uri.getPath();
				try {
					if(filePath!=null)
						FileEncrypt();
				} catch (InvalidKeyException | NoSuchAlgorithmException
						| InvalidKeySpecException | NoSuchPaddingException
						| IOException | InvalidParameterSpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case FILE_SELECT_DECRYPT:
			if(resultcode==RESULT_OK)
			{
				Uri uri=data.getData();
				filePath=uri.getPath();
				try {
					if(filePath!=null)
						FileDerypt();
				} catch (InvalidKeyException | NoSuchAlgorithmException
						| InvalidKeySpecException | NoSuchPaddingException
						| IOException | InvalidAlgorithmParameterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
			break;
		default:
			break;
		}
		super.onActivityResult(requestCode, resultcode, data);
	}
	
	private byte[] ConcateArrays(byte[] first, byte[] second)
	{
		byte[] result=Arrays.copyOf(first, first.length+second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	private void Encrypt() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException
	{
		SecretKeyFactory ckFactory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec keyspec=new PBEKeySpec(textKey.getText().toString().toCharArray(), new byte[] {0,1,2,3,4,5,6,7}, 2048, 256);
		SecretKey myKey=ckFactory.generateSecret(keyspec);
		SecretKey myAESKey=new SecretKeySpec(myKey.getEncoded(), "AES");
		Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, myAESKey);
		byte[] iv=cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
		byte[] ciphertext=cipher.doFinal(plainText.getText().toString().getBytes());
		byte[] CTwithIV=ConcateArrays(iv, ciphertext);
		plainText.setText(Base64.encodeToString(CTwithIV, Base64.DEFAULT));
	}
	
	private void Decrypt() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException
	{
		String password=textKey.getText().toString();
		byte[] cipherTextwithIV=Base64.decode(plainText.getText().toString(), Base64.DEFAULT);
		SecretKeyFactory ckFactory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec keyspec=new PBEKeySpec(textKey.getText().toString().toCharArray(), new byte[] {0,1,2,3,4,5,6,7}, 2048, 256);
		SecretKey myKey=ckFactory.generateSecret(keyspec);
		SecretKey myAESKey=new SecretKeySpec(myKey.getEncoded(), "AES");
		Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] iv=new byte[16];
		System.arraycopy(cipherTextwithIV, 0, iv, 0, 16);
		cipher.init(Cipher.DECRYPT_MODE, myAESKey, new IvParameterSpec(iv));		
		byte[] ciphertext=new byte[cipherTextwithIV.length-16];
		System.arraycopy(cipherTextwithIV, 16, ciphertext, 0, cipherTextwithIV.length-16);
		byte[] message=cipher.doFinal(ciphertext);		
		plainText.setText(new String(message));
	}
	
	private void FileEncrypt() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IOException, InvalidParameterSpecException
	{
		InputStream inStream=new FileInputStream(filePath);
		File outputFile=new File(filePath+".enc");
		int read=0;
		if(!outputFile.exists())
			outputFile.createNewFile();
		FileOutputStream outStream=new FileOutputStream(outputFile);
		SecretKeyFactory ckFactory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec keyspec=new PBEKeySpec(fileKey.getText().toString().toCharArray(), new byte[] {0,1,2,3,4,5,6,7}, 2048, 256);
		SecretKey myKey=ckFactory.generateSecret(keyspec);
		SecretKey myAESKey=new SecretKeySpec(myKey.getEncoded(), "AES");
		Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
		cipher.init(Cipher.ENCRYPT_MODE, myAESKey);
		byte[] iv=cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
		outStream.write(iv);
		CipherOutputStream ciphStream=new CipherOutputStream(outStream, cipher);
		byte[] block=new byte[16];
		//ciphStream.write(iv,0,16);
		while((read=inStream.read(block, 0, 16))!=-1)
		{
			ciphStream.write(block,0,read);			
		}
		ciphStream.close();
		inStream.close();
	}
	
	private void FileDerypt() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException
	{
		InputStream inStream=new FileInputStream(filePath);
		File outputFile=new File(filePath.substring(0, filePath.length()-4));
		int read=0;
		if(!outputFile.exists())
			outputFile.createNewFile();
		FileOutputStream outStream=new FileOutputStream(outputFile);
		SecretKeyFactory ckFactory=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec keyspec=new PBEKeySpec(fileKey.getText().toString().toCharArray(), new byte[] {0,1,2,3,4,5,6,7}, 2048, 256);
		SecretKey myKey=ckFactory.generateSecret(keyspec);
		SecretKey myAESKey=new SecretKeySpec(myKey.getEncoded(), "AES");
		Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
		byte[] iv=new byte[16];
		inStream.read(iv, 0, 16);
		cipher.init(Cipher.DECRYPT_MODE, myAESKey, new IvParameterSpec(iv));
		CipherOutputStream ciphStream=new CipherOutputStream(outStream, cipher);
		byte[] block=new byte[16];
		while((read=inStream.read(block, 0, 16))!=-1)
		{
			ciphStream.write(block,0,read);
		}
		ciphStream.close();
		inStream.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
