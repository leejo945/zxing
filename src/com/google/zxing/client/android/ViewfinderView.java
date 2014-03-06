/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {
   /**
    * ɨ����һ����Ч��
    */
	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192,
			128, 64 };
	private static final long ANIMATION_DELAY = 80L;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 20;
	private static final int POINT_SIZE = 6;

	private CameraManager cameraManager;
	private final Paint paint;
	private Bitmap resultBitmap;
	
	private static final int sp_b = 40;
	private static final int sp_s = 10;
	/**
	 * ɨ����Ľǵ���ɫ
	 */
	
	 
	/**
	 *  ��Ļ������ɫ
	 */
	private final int maskColor;
	/**
	 *    
	 */
	private final int resultColor;
	/**
	 *    ɨ���ߵ���ɫ
	 */
	private final int laserColor;
	/**
	 *  ��˸�ĵ����ɫ
	 */
	private final int resultPointColor;
	private int middle;
	private Paint p;
	 
	private int scannerAlpha;
	private List<ResultPoint> possibleResultPoints;
	private List<ResultPoint> lastPossibleResultPoints;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// Initialize these once for performance rather than calling them every
		// time in onDraw().
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Resources resources = getResources();
	
		maskColor = resources.getColor(R.color.viewfinder_mask);
		 
		resultColor = resources.getColor(R.color.result_view);
	 
		laserColor = resources.getColor(R.color.viewfinder_laser);
		 
		resultPointColor = resources.getColor(R.color.possible_result_points);
		scannerAlpha = 0;
		// ��˸�ĵ�ĸ���
		possibleResultPoints = new ArrayList<ResultPoint>(5);
		lastPossibleResultPoints = null;
		
		  p= new Paint();
	   p.setColor(Color.RED);
		
		
	}

	public void setCameraManager(CameraManager cameraManager) {
		this.cameraManager = cameraManager;
	}

	@Override
	public void onDraw(Canvas canvas) {
		if (cameraManager == null) {
			return; // not ready yet, early draw before done configuring
		}
		Rect frame = cameraManager.getFramingRect();
//		Rect previewFrame = cameraManager.getFramingRectInPreview();
//		if (frame == null || previewFrame == null) {
//			return;
//		}
		if(frame==null){
			return;
		}
		
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		Log.e("zxing", "����������" + width + "----" + height);
		// Draw the exterior (i.e. outside the framing rect) darkened
		paint.setColor(resultBitmap != null ? resultColor : maskColor);
		
		  //frame.top=101
		 //frame.left==180
		 //frame.bottom=438
		
		// ��Ļ����
	
		 canvas.drawRect(0, 0, width, frame.top, paint);
		 canvas.drawRect(frame.left, frame.top,frame.left+sp_b, frame.top+sp_s, p);
		 canvas.drawRect(frame.left, frame.top,frame.left+sp_s, frame.top+sp_b, p);
		 
		// ��Ļ���
		  
		 canvas.drawRect(0, frame.top, frame.left, frame.bottom , paint);
		 canvas.drawRect(frame.left, frame.bottom,frame.left+sp_b, frame.bottom-sp_s, p);
		 canvas.drawRect(frame.left, frame.bottom,frame.left+sp_s, frame.bottom-sp_b, p);
		 
		 
		//��Ļ�ұ�
		canvas.drawRect(frame.right , frame.top, width, frame.bottom ,
				paint);
		
		 canvas.drawRect(frame.right, frame.top,frame.right-sp_b, frame.top+sp_s, p);
		 canvas.drawRect(frame.right, frame.top,frame.right-sp_s, frame.top+sp_b, p);
		 
		
		//��Ļ�±�
		 canvas.drawRect(0, frame.bottom , width, height, paint);
		 
		 canvas.drawRect(frame.right, frame.bottom , frame.right-sp_b, frame.bottom-sp_s, p);
		 canvas.drawRect(frame.right, frame.bottom , frame.right-sp_s, frame.bottom-sp_b, p);
		 //frame.top=101
		 //frame.left==180
		 //frame.bottom=438
	     //frame.right =780
//		if (resultBitmap != null) {
//			//��ɨ�������󣬻�����ɨ������Ķ�ά��
//			// Draw the opaque result bitmap over the scanning rectangle
//			paint.setAlpha(CURRENT_POINT_OPACITY);
//			canvas.drawBitmap(resultBitmap, null, frame, paint);
//		} else {
//          //���û��ɨ��ɨ�����ͻ���ʾɨ����
//			// Draw a red "laser scanner" line through the middle to show
//			// decoding is active
 			paint.setColor(laserColor);
 	    	paint.setAlpha(SCANNER_ALPHA[scannerAlpha]);
 			scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.length;
 			//����ɨ������ɨ�����м�
 			if(middle==0){//��һ��
 				// middle = frame.height() / 2 + frame.top;
 				middle  =frame.top;
 			}else{
 				middle+=10;;
 				if(middle>=frame.bottom){
 					middle = 0;
 				}
 			}
 			 
 			
 			canvas.drawRect(frame.left + 2, middle - 2, frame.right - 1,
 					middle + 2, paint);
 
//			float scaleX = frame.width() / (float) previewFrame.width();
//			float scaleY = frame.height() / (float) previewFrame.height();
  
//			List<ResultPoint> currentPossible = possibleResultPoints;
//			List<ResultPoint> currentLast = lastPossibleResultPoints;
//			int frameLeft = frame.left;
//			int frameTop = frame.top;
//			if (currentPossible.isEmpty()) {
//				lastPossibleResultPoints = null;
//			} else {
//				possibleResultPoints = new ArrayList<ResultPoint>(5);
//				lastPossibleResultPoints = currentPossible;
//				paint.setAlpha(CURRENT_POINT_OPACITY);
//				paint.setColor(resultPointColor);
//				synchronized (currentPossible) {
//					for (ResultPoint point : currentPossible) {
//						canvas.drawCircle(frameLeft
//								+ (int) (point.getX() * scaleX), frameTop
//								+ (int) (point.getY() * scaleY), POINT_SIZE,
//								paint);
//					}
//				}
//			}
//			if (currentLast != null) {
//				paint.setAlpha(CURRENT_POINT_OPACITY / 2);
//				paint.setColor(resultPointColor);
//				synchronized (currentLast) {
//					float radius = POINT_SIZE / 2.0f;
//					for (ResultPoint point : currentLast) {
//						canvas.drawCircle(frameLeft
//								+ (int) (point.getX() * scaleX), frameTop
//								+ (int) (point.getY() * scaleY), radius, paint);
//					}
//				}
//			}

			// Request another update at the animation interval, but only
			// repaint the laser line,
			// not the entire viewfinder mask.
			//ˢ�½���
			postInvalidateDelayed(ANIMATION_DELAY, frame.left - POINT_SIZE,
					frame.top - POINT_SIZE, frame.right + POINT_SIZE,
					frame.bottom + POINT_SIZE);
//		}
	}

	public void drawViewfinder() {
		Bitmap resultBitmap = this.resultBitmap;
		this.resultBitmap = null;
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 * 
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		List<ResultPoint> points = possibleResultPoints;
		synchronized (points) {
			points.add(point);
			int size = points.size();
			if (size > MAX_RESULT_POINTS) {
				// trim it
				points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
			}
		}
	}

}
