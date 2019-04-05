package com.artack2;

import com.artack2.interfaces.DeviceCameraControl;
import com.artack2.interfaces.OrientationProvider;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.LocationPoint;
import com.navigine.naviginesdk.LocationView;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.navigine.naviginesdk.SubLocation;
import com.navigine.naviginesdk.Venue;
import com.navigine.naviginesdk.Zone;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

public class Main extends ApplicationAdapter {
	/** ligdx */
	private SpriteBatch batch;
	private SpriteBatch batch2;
	private ModelBatch mBatch;
	private Texture img;
	private OrientationProvider orientationProvider;
	private DeviceCameraControl cameraControl;
	private Cam camera;
	private BitmapFont font;
	private ModelInstance signInstance;
	private Environment environment;

	/** Navigine */
	//Fields
	public static final String USER_HASH = "46E2-62B2-A3FB-CC8D";
	public static final String  SERVER_URL    = "https://api.navigine.com";
	public static final String  LOCATION_NAME = "Navigine Demo";

	// NavigationThread instance
	NavigationThread mNavigation     = NavigineSDK.getNavigation();

	// Location parameters
	private Location mLocation                 = null;
	private int           mCurrentSubLocationIndex  = -1;
	SubLocation subLoc = null;


		// Device parameters
		private DeviceInfo mDeviceInfo = null; // Current device
		private LocationPoint mPinPoint = null; // Potential device target
		private LocationPoint mTargetPoint = null; // Current device target
		//private RectF mPinPointRect = null;
		//val mPinPointRect = RectF();

		private FreeType.Bitmap mVenueBitmap = null;
		private Venue mTargetVenue = null;
		private Venue mSelectedVenue = null;

		private Zone mSelectedZone = null;

	/** Состояния камеры смартфона*/
	public enum Mode {
		permissionsNotGranted,
		normal,
		prepare,
		preview
	}

	static Mode mode = Mode.normal;
	Main(DeviceCameraControl cameraControl, OrientationProvider orientationProvider ){
	//Main( OrientationProvider orientationProvider ){
		this.cameraControl = cameraControl;
		this.orientationProvider = orientationProvider;
	}

	int longSide;
	int shortSide;

	Vector3 pointA = new Vector3(0,30,20);

    ArrayList<Icon> list;

   /* public void fill() {

		}
		else
		{
			mStatusLabel.setText("Error initializing NavigineSDK! Please, contact technical support");
		}
		// Initializing Navigation library (USER_HASH is your personal security key)
		if (!NavigineSDK.initialize(this, USER_HASH, SERVER_URL))
			Toast.makeText(this, "Unable to initialize Navigation library!",
					Toast.LENGTH_LONG).show();
		if (mPinPoint != null && mPinPoint.subLocation == subLoc.id) {
			final PointF T = mLocationView.getScreenCoordinates(mPinPoint);
		}
	}*/

	@Override
	public void create () {

		longSide = Gdx.graphics.getWidth();
		shortSide = Gdx.graphics.getHeight();
		if(longSide < shortSide){
			longSide = shortSide;
			shortSide = Gdx.graphics.getWidth();
		}

		img = new Texture("badlogic.jpg");

		camera = new Cam(38.7f, 0.2f, 10000f);

		batch = new SpriteBatch();
		batch2 = new SpriteBatch();
		mBatch = new ModelBatch();
		/** Инициализация модели */
		ModelBuilder modelBuilder = new ModelBuilder();
		Model signModel = modelBuilder.createBox(0.1f, 2f, 2f, // - ЗЕЛЕНАЯ OX, направлена на север.
                new Material(ColorAttribute.createDiffuse(Color.GREEN)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
		signInstance = new ModelInstance(signModel);
		signInstance.transform.translate(pointA);

		{
			FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameter.size = (shortSide * 48 / 1080); // Размер шрифта пропорционально экрану
			parameter.color = Color.BLACK;

			// Генерируется шрифт нужного размера из ttf файла
			FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/opensans-bold.ttf"));

			font = generator.generateFont(parameter);
			font.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

			generator.dispose();
		}
		{
			// Освещение
			environment = new Environment();
			environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
			environment.add(new DirectionalLight().set(0.3f, 0.3f, 0.3f, 1, 2, -4));
			environment.add(new DirectionalLight().set(0.1f, 0.1f, 0.1f, -1, 0, 0));
		}

		orientationProvider.start();

		//InputListener inputListener = new InputListener(camera);
		//Gdx.input.setInputProcessor(inputListener);

        // Objects initializing
        list = new ArrayList<Icon>();
        list.add(new Icon(0,0,0,"badlogic.jpg"));
        list.add(new Icon(10,10,10,"badlogic.jpg"));

	}

	@Override
	public void render () {
		if(mode == Mode.preview)
		{
			// Очищаем холст прозрачным цветом, чтобы видеть preview камеры смартфона
			Gdx.gl.glClearColor(0, 0, 0, 0);
			Gdx.gl.glViewport(0, 0,Gdx.graphics.getWidth() ,  Gdx.graphics.getHeight());
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		    Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			camera.update(orientationProvider);

/*
        {
            // Sign rotation. Front to the user
            // Вектор разности положения камеры и таблички
            Vector3 difference = camera.getPosition().cpy(); // - (0,0,0)
            // Ось вращения
            Vector3 cross = difference.cpy().crs(-1, 0, 0);
            // Угол на который осуществляется поворот
            float angle = (float) Math.acos(difference.x / difference.len()) * 180 / MathUtils.PI;
            signInstance.transform.idt();
            signInstance.transform.rotate(cross, angle);
        }
*/
		mBatch.begin(camera);
		mBatch.render(signInstance, environment);
		mBatch.end();
/*
Matrix without manual set

Projection
[0.0018518518|0.0|0.0|-1.0]
[0.0|0.0011148272|0.0|-1.0]
[0.0|0.0|-2.0|-1.0]
[0.0|0.0|0.0|1.0]

Matrix with manual set

Projection
[4.730152|0.0|0.0|-37.841217]
[0.0|2.847583|0.0|-22.780664]
[0.0|0.0|-1.020202|-2.020202]
[0.0|0.0|-1.0|0.0]

*/
		Venue v = new Venue();
        // Позиция таблички относительно камеры (Табличка в 0,0,0)
        Vector3 signPosition = new Vector3(camera.getPosition());
        signPosition.nor();
        signPosition.scl(-1);

        //batch.setProjectionMatrix(tmpMat.set(camera.combined).mul(matrix));
		batch.begin();
		/**  Итератор отрисовки меток*/
        Iterator<Icon> iterator = list.iterator();
        int p=0;
        while(iterator.hasNext()){
            Icon obj = iterator.next();
            Vector3 difference = obj.coord.cpy().sub(camera.position);
            // Скалярное произведение меньше нуля в нужном полупространстве.
            float scl = difference.dot(camera.up);
            if (scl>0)continue;
            Vector3 projection = obj.coord.cpy().prj(camera.combined);
            if(projection.x>-1 && projection.x < 1 && projection.y > -1 && projection.y < 1){
                // Получаем позицию в системе координат экрана
                Vector3 coord = new Vector3(projection.x+1,projection.y+1,0);
                coord.x *= shortSide*0.5f;
                coord.y *= longSide*0.5f;
                font.draw(batch, "Distance", (int)coord.x, (int)coord.y);
                batch.draw(obj.texture, (int)coord.x, (int)coord.y);
            }
            font.draw(batch, String.format(Locale.ENGLISH, "Projection{\n%.2f,\n%.2f,\n%.2f}",projection.x,projection.y,projection.z),(int)(Gdx.graphics.getWidth()*(0.05f+0.2f*p)), (int) (Gdx.graphics.getHeight()*0.95f));
            ++p;
		}

/*        // Скалярное произведение меньше нуля в нужном полупространстве.
		float scl = pointA.dot(camera.up);
        if(scl < 0 && test1.x>-1 && test1.x < 1 && test1.y > -1 && test1.y < 1){
            Vector3 coord = new Vector3(test1.x+1,test1.y+1,0);
            coord.x*=shortSide*0.5f;
            coord.y*=longSide*0.5f;
            font.draw(batch, "Toilet", (int)coord.x, (int)coord.y);
        }
*/


		//font.draw(batch, String.format(Locale.ENGLISH, "Projection{\n%.2f,\n%.2f,\n%.2f}",test1.x,test1.y,test1.z), (int) (Gdx.graphics.getWidth() * 0.05f), (int) (Gdx.graphics.getHeight()*0.95f));
		//font.draw(batch, String.format(Locale.ENGLISH, "Sign{\n%.2f,\n%.2f,\n%.2f}",signPosition.x,signPosition.y,signPosition.z), (int) (Gdx.graphics.getWidth()*0.25f), (int) (Gdx.graphics.getHeight()*0.95f));
        //font.draw(batch, String.format(Locale.ENGLISH, "Cam{\n%.2f,\n%.2f,\n%.2f}",camera.direction.x,camera.direction.y,camera.direction.z), (int) (Gdx.graphics.getWidth()*0.05f), (int) (Gdx.graphics.getHeight()*0.95f));
        batch.end();
		}
		else if (mode == Mode.normal)
		{
				mode = Mode.prepare;
			if (cameraControl != null)
			{
				cameraControl.prepareCameraAsync();
			}
		} else if (mode == Mode.prepare)
		{
			if (cameraControl != null && cameraControl.isReady())
			{
				cameraControl.startPreviewAsync();
				mode = Mode.preview;
			}
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		mBatch.dispose();
		img.dispose();
		orientationProvider.stop();
	}
}
