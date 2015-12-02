package hakashi.ruyka.pdfviewer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;

import hhp.pdfreader.R;

public class PDFView extends Activity {
    private File document;
    private ImageView imageView1, imageView2;
    private Button pageView;
    private int totalPage;
    private int pos;
    private Intent lastPage;
    private Animation swipeLeft, swipeRight, fade;
    private Bitmap curImage, oldImage;

    // Image view prosperity
    private int REQ_WIDTH, REQ_HEIGHT, currentPage;
    private Rect rect;

    // move, drag, zoom handler
    private static final float MIN_ZOOM = 0.5f, MAX_ZOOM = 1.5f;
    private float[] currValues = new float[9];
    private Matrix m = new Matrix();
    private Matrix savedM = new Matrix();
    private boolean isZoomed;
    private boolean isTouched;

    // record user touching
    private PointF start = new PointF();
    private PointF last = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;

    // 4 event states
    static final int NONE = 0;
    static final int SWIPE = 2;
    static final int DRAG = 3;
    static final int ZOOM = 4;
    int mode = NONE;

    // PDF renderer
    private PdfRenderer renderer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_view);

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("pdf_path");
        pos = intent.getIntExtra("position", 0);
        currentPage = intent.getIntExtra("last_page", 0);


        document = new File(filePath);

        init();

        imageView1.post(new Runnable() {
            @Override
            public void run() {
                curImage = render();
                imageView1.setImageBitmap(curImage);
                imageView1.setImageMatrix(m);
                imageView1.invalidate();
            }
        });

        pageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(PDFView.this);
                dialog.setContentView(R.layout.page_dialog);
                dialog.setTitle("Page");
                final EditText editText = (EditText) dialog.findViewById(R.id.page);
                TextView tvPage = (TextView) dialog.findViewById(R.id.totalPage);
                tvPage.setText("/" + Integer.toString(totalPage));
                Button go = (Button) dialog.findViewById(R.id.go);
                Button cancle = (Button) dialog.findViewById(R.id.cancle);
                cancle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                go.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            int temp = Integer.parseInt(editText.getText().toString());
                            if(temp > 0 && temp <= totalPage){
                                currentPage = temp - 1;
                                curImage = render();
                                imageView1.setImageBitmap(curImage);
                                imageView1.setImageMatrix(m);
                                imageView1.invalidate();
                            }
                        } catch (NumberFormatException e){
                            e.printStackTrace();
                        }
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });

        imageView1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                view.setScaleType(ImageView.ScaleType.MATRIX);

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        isTouched = true;
                        savedM.set(m);
                        start.x = event.getX();
                        start.y = event.getY();
                        if (isZoomed)
                            mode = DRAG;
                        else
                            mode = SWIPE;
                        break;

                    case MotionEvent.ACTION_UP:

                    case MotionEvent.ACTION_POINTER_UP:
                        if (mode == SWIPE)
                            onSwipeEvent(event);
                        mode = NONE;
                        onMotionStop();
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);
                        if (oldDist > 5f) {
                            savedM.set(m);
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        isTouched = false;
                        if (mode == SWIPE) {
                            last.set(event.getX(), event.getY());
                        } else if (mode == DRAG)
                            onDRAG(event);
                        else if (mode == ZOOM)
                            onZOOM(event);
                        break;
                }

                view.setImageMatrix(m);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        lastPage = new Intent();
        lastPage.putExtra("last_page", currentPage);
        lastPage.putExtra("position", pos);
        setResult(RESULT_OK, lastPage);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        renderer.close();
        super.onPause();
    }

    @Override
    protected void onResume() {
        try {
            if(renderer == null)
                renderer = new PdfRenderer(ParcelFileDescriptor.open(document, ParcelFileDescriptor.MODE_READ_ONLY));
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    private void setDefault() {
        savedM.reset();
        m.reset();
        m.getValues(currValues);
        start.set(0, 0);
        mid.set(0, 0);
        imageView1.setImageMatrix(m);
    }

    private void init() {
        rect = new Rect();
        pageView = (Button) findViewById(R.id.page);
        swipeLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_left);
        swipeRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_right);
        swipeLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView1.setImageBitmap(curImage);
                imageView1.setImageMatrix(m);
                imageView1.invalidate();
                imageView1.startAnimation(fade);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        swipeRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView1.setImageBitmap(curImage);
                imageView1.setImageMatrix(m);
                imageView1.invalidate();
                imageView1.startAnimation(fade);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        try {
            renderer = new PdfRenderer(ParcelFileDescriptor.open(document, ParcelFileDescriptor.MODE_READ_ONLY));
            totalPage = renderer.getPageCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView1 = (ImageView) findViewById(R.id.image1);
        imageView1.setScaleType(ImageView.ScaleType.MATRIX);
        imageView2 = (ImageView) findViewById(R.id.image2);
        imageView2.setScaleType(ImageView.ScaleType.MATRIX);
    }

    private void onMotionStop() {
        if (isZoomed) {
            float[] bitmapScaled = new float[2];
            bitmapScaled[0] = REQ_WIDTH*currValues[Matrix.MSCALE_X];
            bitmapScaled[1] = REQ_HEIGHT*currValues[Matrix.MSCALE_Y];

            float[] botRightBound = new float[2];
            botRightBound[0] = imageView1.getWidth() - bitmapScaled[0];
            botRightBound[1] = imageView1.getHeight() - bitmapScaled[1];


            if (currValues[Matrix.MTRANS_X] > 0f) {
                currValues[Matrix.MTRANS_X] = 0;
                m.setValues(currValues);
            }

            if (currValues[Matrix.MTRANS_Y] > 0f) {
                if(bitmapScaled[1] > imageView1.getHeight()) {
                    currValues[Matrix.MTRANS_Y] = 0;
                    m.setValues(currValues);
                } else if(currValues[Matrix.MTRANS_Y] > botRightBound[1]){
                    currValues[Matrix.MTRANS_Y] = botRightBound[1];
                    m.setValues(currValues);
                }
            }

            if(currValues[Matrix.MTRANS_X] < botRightBound[0]) {
                currValues[Matrix.MTRANS_X] = botRightBound[0];
                m.setValues(currValues);
            }

            if(currValues[Matrix.MTRANS_Y] < botRightBound[1]) {
                if(bitmapScaled[1] > imageView1.getHeight()) {
                    currValues[Matrix.MTRANS_Y] = botRightBound[1];
                    m.setValues(currValues);
                } else if(currValues[Matrix.MTRANS_Y] < 0f){
                    currValues[Matrix.MTRANS_Y] = 0;
                    m.setValues(currValues);
                }
            }


        } else {
            if(currValues[Matrix.MTRANS_X] != 0 || currValues[Matrix.MTRANS_Y] != 0) {
                currValues[Matrix.MTRANS_X] = currValues[Matrix.MTRANS_Y] = 0;
                m.setValues(currValues);
                float temp = (imageView1.getHeight() - REQ_HEIGHT*currValues[Matrix.MSCALE_Y])/ 2f;
                m.postTranslate(0, temp);
                m.getValues(currValues);
            }
        }
    }

    private void onSwipeEvent(MotionEvent event) {
        oldImage = curImage;
        Handler handler = new Handler();
        if(isTouched) {
            if (event.getX() < imageView1.getWidth() / 2f) {
                if (currentPage > 0) {
                    currentPage--;
                    curImage = render();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            imageView1.setImageResource(android.R.color.transparent);
                            imageView2.setImageBitmap(oldImage);
                            imageView2.setImageMatrix(m);
                            imageView2.invalidate();
                            imageView2.startAnimation(swipeRight);
                        }
                    };
                    handler.postDelayed(runnable, 100);
                }
            } else if (event.getX() > imageView1.getWidth() / 2f) {
                if (currentPage < totalPage - 1) {
                    currentPage++;
                    curImage = render();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            imageView1.setImageResource(android.R.color.transparent);
                            imageView2.setImageBitmap(oldImage);
                            imageView2.setImageMatrix(m);
                            imageView2.invalidate();
                            imageView2.startAnimation(swipeLeft);
                        }
                    };
                    handler.postDelayed(runnable, 100);
                }
            }
        }else{
            if (last.x > start.x) {
                if (currentPage > 0) {
                    currentPage--;
                    curImage = render();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            imageView1.setImageResource(android.R.color.transparent);
                            imageView2.setImageBitmap(oldImage);
                            imageView2.setImageMatrix(m);
                            imageView2.invalidate();
                            imageView2.startAnimation(swipeRight);
                        }
                    };
                    handler.postDelayed(runnable, 100);
                }
            } else if (last.x < start.x) {
                if (currentPage < totalPage - 1) {
                    currentPage++;
                    curImage = render();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            imageView1.setImageResource(android.R.color.transparent);
                            imageView2.setImageBitmap(oldImage);
                            imageView2.setImageMatrix(m);
                            imageView2.invalidate();
                            imageView2.startAnimation(swipeLeft);
                        }
                    };
                    handler.postDelayed(runnable, 100);
                }
            }
        }
    }

    private void onDRAG(MotionEvent event){
        m.set(savedM);
        m.postTranslate(event.getX() - start.x, event.getY() - start.y);
        m.getValues(currValues);
    }

    private void onZOOM(MotionEvent event) {
        float scale;
        float newDist = spacing(event);
        if (newDist > 5f) {
            m.set(savedM);
            scale = newDist / oldDist;
            m.postScale(scale, scale, mid.x, mid.y);
            isZoomed = true;
            m.getValues(currValues);
            if (currValues[Matrix.MSCALE_X] >= MAX_ZOOM) {
                currValues[Matrix.MSCALE_X] = currValues[Matrix.MSCALE_Y] = 1f;
                m.setValues(currValues);
            } else if (currValues[Matrix.MSCALE_X] <= MIN_ZOOM) {
                currValues[Matrix.MSCALE_X] = currValues[Matrix.MSCALE_Y] = 0.5f;
                isZoomed = false;
                m.setValues(currValues);
            }
        }
    }

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void setup(PdfRenderer.Page page) {
        setDefault();
        REQ_WIDTH = imageView1.getWidth() * 2;
        REQ_HEIGHT = ((int) ((float) page.getHeight() * REQ_WIDTH) / page.getWidth());
        rect.set(0, 0, REQ_WIDTH, REQ_HEIGHT);
        m.postScale((float) 1 / 2, (float) 1 / 2);
        m.getValues(currValues);
        float temp = (imageView1.getHeight() - REQ_HEIGHT * currValues[Matrix.MSCALE_Y]) / 2f;
        m.postTranslate(0, temp);
        m.getValues(currValues);
    }

    private Bitmap render() {
        try {
            PdfRenderer.Page page = renderer.openPage(currentPage);
            setup(page);
            Bitmap bitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            page.render(bitmap, rect, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            pageView.setText(Integer.toString(currentPage + 1) + "/" + Integer.toString(totalPage));
            return bitmap;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}