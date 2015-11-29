package ca.yorku.cse.mack.demotiltballcse03076;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
* DemoAndroid - with modifications by...
*
* Login ID - CSE03076
* Student ID - 210635597
* Last name - Likhite
* First name(s) - Rohan
*/

public class RollingBallPanel extends View
{
    /*SCENARIOS[][][]:
    Contém os cenários do jogo. Como está descrito abaixo, cada
    cenário tem a descrição dos quadrados (só os vermelhos) que
    o compoem. As coordenadas estão organizadas de acordo com o
    seguinte padrão:

    {L,T,R,B} = L: left, T: top, R: right, B: bottom.

    As coordenadas devem ser armazenadas seguindo ese padrão ou
    o quadrado não será desenhado ou será desenhado errado.

               Top              Plano cartesiano da tela do celular:
            _________               +-------------------------> +X
            |       |          =>   |                       |
    Left    |       |   Right  =>   |    tela do celular    |
            |_______|          =>   |                       |
             Bottom                 ↓ +Y                    |

    No meu celular (Moto E) as dimensões da tela são:
    x: [0, 540]     y: [0,888]

    NÃO colocar quadrados vermelhos nos intervalos:
    x:[0,180] & y:[0,180] //quadrado verde do canto superior esquerdo
    x:[360,540] & y:[708,888] //quadrado verde do canto inferior direito
    isso é pra evitar que tenha quadrados vermelhos muito perto dos verdes
    */
    final float SCENARIOS[][][] =
    { //scenarios
        { //scenario 1
            {250,350,350,450}, //scenario 1: square 1 coordinates
        },
        { //scenario 2
            {300,20,400,40}, //scenario 2: square 1 coordinates
            {200,20,300,800}, //scenario 2: square 2 coordinates
            {400,600,450,650}  //scenario 2: square 3 coordinates
        },
        { //scenario 3
            {0,0,0,0},
            {0,0,0,0},
            {0,0,0,0}
        }
    };//NÃO SE ESQUEÇA DE ATUALIZAR AS FLAGS ABAIXO
    final int NUM_SCENARIOS = 3; //número de cenários existentes (no momento, 3)
    final int SQUARES[] = {1,3,3}; //número de quadrados de cada cenário (no momento o cenario 1 tem 1 e o resto tem 3)
    int chosenScenario; //cenário escolhido para ser desenhado

	final static String MYDEBUG = "MYDEBUG"; // for Log.i messages
	final float DEGREES_TO_RADIANS = 0.0174532925f;
	final int DEFAULT_BALL_DIAMETER = 10;
	
	// the ball diameter will be min(screenWidth, screenHeight) / this_value
	final float BALL_DIAMETER_ADJUST_FACTOR = 15;

	final int DEFAULT_LABEL_TEXT_SIZE = 200; // tweak as necessary
	final int DEFAULT_STATS_TEXT_SIZE = 30;
	final int DEFAULT_GAP = 7; // between lines of text
	final int DEFAULT_OFFSET = 10; // from bottom of display

	final int MODE_NONE = 0;
	final int PATH_TYPE_SQUARE = 1;
	final int PATH_TYPE_CIRCLE = 2;

	final float PATH_WIDTH_NARROW = 2.0f; // ... x ball diameter
	final float PATH_WIDTH_MEDIUM = 4.0f; // ... x ball diameter
	final float PATH_WIDTH_WIDE = 8.0f; // ... x ball diameter
	final float BALL_START_X = 100;
	final float BALL_START_Y = 100;
	final float FINISH_SQUARE_A[] = {125, -300, 225, -400}; //left, top, right, bottom
	final float FINISH_SQUARE_B[] = {-225, 400, -125, 300}; //left, top, right, bottom
	float finishSquare[];
	int levelCleared = 0;
	boolean tapToExit = false;
	boolean tapToStart = false; //locks the ball position on start of level, player must tap screen to start
	boolean timeStop = false; //if true, ball does not move
	int currentLevel; //contador de nível do jogo (cada vez que toca no quadrado verde = +1 level)
	//~~~~~~~
    Random random;

	//Check Laps
//	boolean lapStart = false;
//	boolean lapCheckOneBool = false;
//	boolean lapDirection = false;
//	int lapCount = 0;
	String totalLaps;

	int pathType;
	float radiusOuter, radiusInner;

	Bitmap ball, temp;
	int ballDiameter;

	float dT; // time since last sensor event (seconds)

	float screenWidth, screenHeight, scalingFactor;
	int gap, offset;

	RectF dangerRectangle, finishRectangle, screenBorderRectangle, innerShadowRectangle, ballNow, lineStart, lapCheckOne, lapDirectionCheck;
	float pathWidth;
	boolean touchFlag;
	Vibrator vib;

	float xBall, yBall; // top-left of the ball (for painting)
	float xBallCenter, yBallCenter; // center of the ball

	float pitch, roll;
	float tiltAngle, tiltMagnitude;
	String orderOfControl;
	float gain;
	float velocity; // in pixels/second (velocity = tiltMagnitude * tiltVelocityGain
	float dBall; // the amount to move the ball (in pixels): dBall = dT * velocity
	float xCenter, yCenter; // the center of the screen
	long now, lastT;
	Paint statsPaint, labelPaint, dangerLinePaint, dangerFillPaint, finishLinePaint, finishFillPaint, screenBorderPaint;
	int labelColor = 0x00ffffff;

	public RollingBallPanel(Context contextArg)
	{
		super(contextArg);
		initialize();
	}

	public RollingBallPanel(Context contextArg, AttributeSet attrs)
	{
		super(contextArg, attrs);
		initialize();
	}

	public RollingBallPanel(Context contextArg, AttributeSet attrs, int defStyle)
	{
		super(contextArg, attrs, defStyle);
		initialize();
	}

	// things that can be initialized from within this View
	private void initialize()
	{
        random = new Random();
		finishSquare = FINISH_SQUARE_A;

		finishLinePaint = new Paint();
		finishLinePaint.setColor(0xff6ab64a);
		finishLinePaint.setStyle(Paint.Style.STROKE);
		finishLinePaint.setStrokeWidth(5);
		finishLinePaint.setAntiAlias(true);

		finishFillPaint = new Paint();
		finishFillPaint.setColor(0xffc3ffaa);
		finishFillPaint.setStyle(Paint.Style.FILL);

		dangerLinePaint = new Paint();
		dangerLinePaint.setColor(Color.RED);
		dangerLinePaint.setStyle(Paint.Style.STROKE);
		dangerLinePaint.setStrokeWidth(5);
		dangerLinePaint.setAntiAlias(true);

		dangerFillPaint = new Paint();
		dangerFillPaint.setColor(0xfffd9494);
		dangerFillPaint.setStyle(Paint.Style.FILL);

		screenBorderPaint = new Paint();
		screenBorderPaint.setColor(Color.DKGRAY);
		screenBorderPaint.setStyle(Paint.Style.STROKE);
		screenBorderPaint.setStrokeWidth(5);
		screenBorderPaint.setAntiAlias(true);

		labelPaint = new Paint();
		labelPaint.setColor(Color.WHITE);
		labelPaint.setTextSize(DEFAULT_LABEL_TEXT_SIZE);
		labelPaint.setAntiAlias(true);

		statsPaint = new Paint();
		statsPaint.setColor(Color.WHITE);
		statsPaint.setTextSize(DEFAULT_STATS_TEXT_SIZE);
		statsPaint.setAntiAlias(true);

		lastT = System.nanoTime();

		ballDiameter = DEFAULT_BALL_DIAMETER;
		temp = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
		ball = Bitmap.createScaledBitmap(temp, ballDiameter, ballDiameter, true);

		this.setBackgroundColor(Color.BLACK);

		pathWidth = PATH_WIDTH_MEDIUM * ballDiameter; // default
		touchFlag = false;

		lineStart = new RectF();
		lapCheckOne  = new RectF();
		lapDirectionCheck  = new RectF();
		finishRectangle = new RectF();
		dangerRectangle = new RectF();
		innerShadowRectangle = new RectF();
		screenBorderRectangle = new RectF();
		ballNow = new RectF();
		currentLevel = 0;
	}

	public void setGain(float gainArg)
	{
		gain = gainArg;
	}

	public void setOrderOfControl(String orderOfControlArg)
	{
		orderOfControl = orderOfControlArg;
	}

	public void settotalLaps(String totalLapsArg)
	{
		totalLaps = totalLapsArg;
	}

	/*
	 * Do the heavy lifting here! Update the ball position based on the tilt angle, tilt
	 * magnitude, order of control, etc.
	 */
	public void updateBallPosition(float pitchArg, float rollArg, float tiltAngleArg, float tiltMagnitudeArg) {

		pitch = pitchArg; // for information only (see onDraw)
		roll = rollArg; // for information only (see onDraw)
		tiltAngle = tiltAngleArg;
		tiltMagnitude = tiltMagnitudeArg;

		// get current time and delta since last onDraw
		now = System.nanoTime();
		dT = (now - lastT) / 1000000000f; // seconds
		lastT = now;

		// don't allow tiltMagnitude to exceed 30 degrees
		final float MAX_MAGNITUDE = 30f;
		tiltMagnitude = tiltMagnitude > MAX_MAGNITUDE ? MAX_MAGNITUDE : tiltMagnitude;

		// This is the only code that distinguishes velocity-control from position-control
		if (orderOfControl.equals("Velocity")) // velocity control
		{
			if (!timeStop) {
				// compute how far the ball should move
				velocity = tiltMagnitude * gain;
				dBall = dT * velocity; // make the ball move this amount (pixels)

				// compute the ball's new coordinates
				float dx = (float) Math.sin(tiltAngle * DEGREES_TO_RADIANS) * dBall;
				float dy = (float) Math.cos(tiltAngle * DEGREES_TO_RADIANS) * dBall;
				xBall += dx;
				yBall += dy;
			}
		} else
		// position control
		{
			// compute how far the ball should move
			dBall = tiltMagnitude * gain;

			// compute the ball's new coordinates
			float dx = (float)Math.sin(tiltAngle * DEGREES_TO_RADIANS) * dBall;
			float dy = (float)Math.cos(tiltAngle * DEGREES_TO_RADIANS) * dBall;
			xBall = xCenter + dx;
			yBall = yCenter + dy;
		}

		// keep the ball visible (also, restore if NaN)
		if (Float.isNaN(xBall) || xBall < 0)
			xBall = 0;
		else if (xBall > screenWidth - ballDiameter)
			xBall = screenWidth - ballDiameter;
		if (Float.isNaN(yBall) || yBall < 0)
			yBall = 0;
		else if (yBall > screenHeight - ballDiameter)
			yBall = screenHeight - ballDiameter;

		xBallCenter = xBall + ballDiameter / 2f;
		yBallCenter = yBall + ballDiameter / 2f;

		//bola acerta quadrado verde = WIN (avança 1 level)
		if (ballTouchingLine() == 1 && !touchFlag && levelCleared != -1)
		{
//			touchFlag = true;
//			vib.vibrate(10); // 10 ms vibrotactile pulse
//			timeStop = true;
//			invalidate();
            chosenScenario = random.nextInt(NUM_SCENARIOS); //nº entre 0 e NUM_SCENARIOS-1

			++currentLevel;
			levelCleared = 1;
			labelColor = 0xffffffff;
			if (finishSquare == FINISH_SQUARE_A)
				finishSquare = FINISH_SQUARE_B;
			else
				finishSquare = FINISH_SQUARE_A;

			finishRectangle.left = xCenter + finishSquare[0];
			finishRectangle.top = yCenter - finishSquare[1];
			finishRectangle.right = xCenter + finishSquare[2];
			finishRectangle.bottom = yCenter - finishSquare[3];

		}//bola acerta quadrado vermelho = LOSE
		else if (ballTouchingLine() == -1 && !touchFlag)
		{
			touchFlag = true;
			vib.vibrate(10); // 10 ms vibrotactile pulse
			levelCleared = -1;
			timeStop = true;
			invalidate();
		}//bola encostou no quadrado vermelho, esperando toque para voltar ao menu
		else if (ballTouchingLine() == 0 && touchFlag && tapToExit) {
			touchFlag = false;
			Intent i = new Intent(this.getContext(), DemoTiltBallSetup.class);
			this.getContext().startActivity(i); //retorna ao menu principal
		}

//		 if(lapStart == false && lapCheckOneBool == false && RectF.intersects(ballNow,lineStart)){
//			lapStart = true;
//			Log.i(MYDEBUG, "Start LAP");
//
//		}
//
//		if(lapStart == true && lapCheckOneBool == false && RectF.intersects(ballNow,lapCheckOne)){
//			lapStart = true;
//			lapCheckOneBool = true;
//			Log.i(MYDEBUG, "Lap Check");
//		}
//
//		if(lapStart == true && lapCheckOneBool == true && RectF.intersects(ballNow,lapDirectionCheck)){
//			lapDirection = true;
//		}
//
//		if(lapStart == true && lapDirection == true && lapCheckOneBool == true && RectF.intersects(ballNow,lineStart)){
//			lapStart = true;
//			lapDirection = false;
//			lapCheckOneBool = false;
//			lapCount ++;
//			Log.i(MYDEBUG, "NEW LAP");
//		}

		invalidate(); // force onDraw to redraw the screen with the ball in its new position
	}

	protected void onDraw(Canvas canvas)
	{
        int i;
		//Lap Start
//		lineStart.left = finishRectangle.left;
//		lineStart.top = (float) (canvas.getHeight() * 0.5);
//		lineStart.right = dangerRectangle.left;
//		lineStart.bottom = (float)(canvas.getHeight() * 0.5 + 2);

		//DirectionCheck
//		lapDirectionCheck.left = finishRectangle.left;
//		lapDirectionCheck.top = (float) (canvas.getHeight() * 0.5 - 5);
//		lapDirectionCheck.right = dangerRectangle.left;
//		lapDirectionCheck.bottom = (float)(canvas.getHeight() * 0.5 + 2);

		//Lap Check
//		lapCheckOne.left = dangerRectangle.right;
//		lapCheckOne.top = (float) (canvas.getHeight() * 0.5);
//		lapCheckOne.right = finishRectangle.right;
//		lapCheckOne.bottom = (float) (canvas.getHeight() * 0.5 + 2);



		// draw the paths
		if (pathType == PATH_TYPE_SQUARE)
		{
			// draw screen border
			canvas.drawRect(screenBorderRectangle, screenBorderPaint);

			// draw finish square
			canvas.drawRect(finishRectangle, finishFillPaint);
			canvas.drawRect(finishRectangle, finishLinePaint);

			// draw danger square
//			canvas.drawRect(dangerRectangle, dangerLinePaint);
//			canvas.drawRect(dangerRectangle, dangerFillPaint);

            for (i=0; i<SQUARES[chosenScenario]; i++) {
                //desenha bordas
                canvas.drawRect(
                        SCENARIOS[chosenScenario][i][0],
                        SCENARIOS[chosenScenario][i][1],
                        SCENARIOS[chosenScenario][i][2],
                        SCENARIOS[chosenScenario][i][3],
                        dangerLinePaint);
                //desenha preenchimento
                canvas.drawRect(
                        SCENARIOS[chosenScenario][i][0],
                        SCENARIOS[chosenScenario][i][1],
                        SCENARIOS[chosenScenario][i][2],
                        SCENARIOS[chosenScenario][i][3],
                        dangerFillPaint);
            }
		}
//		else if (pathType == PATH_TYPE_CIRCLE)
//		{
//			// draw fills
//			canvas.drawOval(finishRectangle, finishFillPaint);
//			canvas.drawOval(dangerRectangle, dangerFillPaint);
//
//			// draw lines
//			canvas.drawOval(finishRectangle, finishLinePaint);
//			canvas.drawOval(dangerRectangle, dangerLinePaint);
//		}


		//Lap Start Line and Arrow
//		canvas.drawRect(lineStart, finishLinePaint);
//		canvas.drawLine(finishRectangle.left - 30, canvas.getHeight()/2 - 30, finishRectangle.left -30, canvas.getHeight()/2 + 40, finishLinePaint);
//		canvas.drawLine(finishRectangle.left - 40, canvas.getHeight()/2 - 10, finishRectangle.left - 30, canvas.getHeight()/2 + 40, finishLinePaint);
//		canvas.drawLine(finishRectangle.left - 20, canvas.getHeight()/2 - 10, finishRectangle.left - 30, canvas.getHeight()/2 + 40, finishLinePaint);
//
//		canvas.drawRect(lapCheckOne, finishLinePaint);
//		canvas.drawRect(lapDirectionCheck, finishLinePaint);

//		// draw label
//		canvas.drawText("Demo Tilt Ball CSE03076", 6, labelTextSize, labelPaint);
//
//		// draw stats (pitch, roll, tilt angle, tilt magnitude)
////		if (pathType == PATH_TYPE_SQUARE || pathType == PATH_TYPE_CIRCLE)
//		{
//			//canvas.drawText("Wall hits = " + wallHits, 6f, screenHeight - offset - 5f * (statsTextSize + gap),statsPaint);

        canvas.drawText("Level "+ currentLevel, screenWidth - 165, screenHeight/10f - 50, statsPaint);
        //debug :P
        canvas.drawText("Screen Width: " + screenWidth, 0, screenHeight/10f - 50, statsPaint);
        canvas.drawText("Screen Height: " + screenHeight, 0, screenHeight/10f - 25, statsPaint);
        canvas.drawText("Scenario: " + chosenScenario, 0, screenHeight/10f + 0, statsPaint);
        canvas.drawText("Ball position: " + (int)xBallCenter + " " + (int)yBallCenter, 0, screenHeight/10f + 25, statsPaint);

		if (labelColor != 0x00ffffff) {
			labelPaint.setColor(labelColor);
			canvas.drawText("Level", 0, screenHeight / 3f, labelPaint);
			canvas.drawText("Up!", 0, 2 * screenHeight / 3f, labelPaint);
			labelColor -= 0x05000000;
		}
		if (levelCleared == -1) {
			labelPaint.setColor(0xffdd9494);
			canvas.drawText("Game", 0, screenHeight / 3f, labelPaint);
			canvas.drawText("Over!", 0, 2 * screenHeight / 3f, labelPaint);
		}

//			canvas.drawText("-----------------", 6f, screenHeight - offset - 4f * (statsTextSize + gap), statsPaint);
//		}
//
//
//		canvas.drawText("Tablet pitch (degrees) = " + trim(pitch, 2), 6f, screenHeight - offset - 3f
//				* (statsTextSize + gap), statsPaint);
//
//		canvas.drawText("Tablet roll (degrees) = " + trim(roll, 2), 6f, screenHeight - offset - 2f
//				* (statsTextSize + gap), statsPaint);
//
//		canvas.drawText("Ball x = " + trim(xBallCenter, 2), 6f, screenHeight - offset - 1f * (statsTextSize + gap),
//				statsPaint);
//
//		canvas.drawText("Ball y = " + trim(yBallCenter, 2), 6f, screenHeight - offset - 0f * (statsTextSize + gap),
//				statsPaint);


		// draw the ball in its new location
		canvas.drawBitmap(ball, xBall, yBall, null);

	} // end onDraw

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int widthMeasureSpec)
	{
		return (int)screenWidth;
	}

	private int measureHeight(int heightMeasureSpec)
	{
		return (int)screenHeight;
	}

	/*
	 * Configure the rolling ball panel according to screen size, path parameters, etc.
	 */
	public void configure(float w, float h, float scalingFactorArg, String pathMode, String pathWidthArg)
	{
		screenWidth = w;
		screenHeight = h;
		scalingFactor = scalingFactorArg;

		if (pathMode.equals("Square"))
			pathType = PATH_TYPE_SQUARE;
		else if (pathMode.equals("Circle"))
			pathType = PATH_TYPE_CIRCLE;
		else
			pathType = MODE_NONE;

		if (pathWidthArg.equals("Narrow"))
			pathWidth = PATH_WIDTH_NARROW;
		else if (pathWidthArg.equals("Wide"))
			pathWidth = PATH_WIDTH_WIDE;
		else
			pathWidth = PATH_WIDTH_MEDIUM;

		xCenter = w / 2f;
		yCenter = h / 2f;
		xBall = BALL_START_X;
		yBall = BALL_START_Y;
		xBallCenter = xBall + ballDiameter / 2f;
		yBallCenter = yBall + ballDiameter / 2f;

		ballDiameter = screenWidth < screenHeight ? (int)(screenWidth / BALL_DIAMETER_ADJUST_FACTOR)
				: (int)(screenHeight / BALL_DIAMETER_ADJUST_FACTOR);

		ball = Bitmap.createScaledBitmap(temp, ballDiameter, ballDiameter, true);

		//border
		screenBorderRectangle.left = 0;
		screenBorderRectangle.top = 0;
		screenBorderRectangle.right = screenWidth - 1;
		screenBorderRectangle.bottom = screenHeight - 1;

		radiusOuter = screenWidth < screenHeight ? 0.40f * screenWidth : 0.40f * screenHeight;
		//green square level 0
		finishRectangle.left = xCenter + finishSquare[0];
		finishRectangle.top = yCenter - finishSquare[1];
		finishRectangle.right = xCenter + finishSquare[2];
		finishRectangle.bottom = yCenter - finishSquare[3];

		// NOTE: path width is 4 x ball diameter
		radiusInner = radiusOuter - pathWidth * ballDiameter;

		//red square level 0
		dangerRectangle.left = xCenter - radiusInner;
		dangerRectangle.top = yCenter - radiusInner;
		dangerRectangle.right = xCenter + radiusInner;
		dangerRectangle.bottom = yCenter + radiusInner;
//
//		// NOTE: line thickness (aka stroke width) is 2
//		outerShadowRectangle.left = finishRectangle.left + ballDiameter - 2f;
//		outerShadowRectangle.top = finishRectangle.top + ballDiameter - 2f;
//		outerShadowRectangle.right = finishRectangle.right - ballDiameter + 2f;
//		outerShadowRectangle.bottom = finishRectangle.bottom - ballDiameter + 2f;
//
//		innerShadowRectangle.left = dangerRectangle.left + ballDiameter - 2f;
//		innerShadowRectangle.top = dangerRectangle.top + ballDiameter - 2f;
//		innerShadowRectangle.right = dangerRectangle.right - ballDiameter + 2f;
//		innerShadowRectangle.bottom = dangerRectangle.bottom - ballDiameter + 2f;
//
//		labelTextSize = (int)(DEFAULT_LABEL_TEXT_SIZE * scalingFactor + 0.5f);
//		labelPaint.setTextSize(labelTextSize);
//
//		statsTextSize = (int)(DEFAULT_STATS_TEXT_SIZE * scalingFactor + 0.5f);
//		statsPaint.setTextSize(statsTextSize);

		gap = (int)(DEFAULT_GAP * scalingFactor + 0.5f);
		offset = (int)(DEFAULT_OFFSET * scalingFactor + 0.5f);
	}

	public void setVibrator(Vibrator v)
	{
		vib = v;
	}

	// trim and round a float to the specified number of decimal places
	private float trim(float f, int decimalPlaces)
	{
		return (int)(f * 10 * decimalPlaces + 0.5f) / (float)(10 * decimalPlaces);
	}

	// returns true if the ball is touching the line of the inner or outer square/circle
	public int ballTouchingLine()
	{
        int i, status = 0;

		if (pathType == PATH_TYPE_SQUARE)
		{
//			ballNow.left = xBall;
//			ballNow.top = yBall;
//			ballNow.right = xBall + ballDiameter;
//			ballNow.bottom = yBall + ballDiameter;
			ballNow.left = xBallCenter;
			ballNow.top = yBallCenter;
			ballNow.right = xBallCenter + 1;
			ballNow.bottom = yBallCenter + 1;

			if (RectF.intersects(ballNow, finishRectangle))
                status = 1; // touching outside square

//			if (RectF.intersects(ballNow, dangerRectangle))
//				return -1; // touching inside square

            for (i=0; i<SQUARES[chosenScenario]; i++) {
                dangerRectangle.left = SCENARIOS[chosenScenario][i][0];
                dangerRectangle.top = SCENARIOS[chosenScenario][i][1];
                dangerRectangle.right = SCENARIOS[chosenScenario][i][2];
                dangerRectangle.bottom = SCENARIOS[chosenScenario][i][3];
                if (RectF.intersects(ballNow, dangerRectangle))
                    status = -1;
            }
            return status;
		}

		else if (pathType == PATH_TYPE_CIRCLE)
		{
			final float ballDistance = (float)Math.sqrt((xBallCenter - xCenter) * (xBallCenter - xCenter)
					+ (yBallCenter - yCenter) * (yBallCenter - yCenter));

			if (Math.abs(ballDistance - radiusOuter) < (ballDiameter / 2f))
				return 1; // touching outer circle

			if (Math.abs(ballDistance - radiusInner) < (ballDiameter / 2f))
				return -1; // touching inner circle
		}
		return status;
	}
}
