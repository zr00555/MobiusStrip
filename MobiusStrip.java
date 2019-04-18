//Zachary Reese
//eID: 900893107

package project10;

import java.applet.Applet;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.media.j3d.*;
import javax.swing.JFrame;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.universe.SimpleUniverse;

public class MobiusStrip extends Applet {

	public static void main(String[] args) {
		new MainFrame(new MobiusStrip(), 800, 600);
	}

	// Create simple universe
	public void init() {
		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
		Canvas3D cv = new Canvas3D(gc);
		this.setLayout(new BorderLayout());
		this.add(cv, BorderLayout.CENTER);
		BranchGroup bg = createSceneGraph();
		bg.compile();
		SimpleUniverse su = new SimpleUniverse(cv);
		su.getViewingPlatform().setNominalViewingTransform();
		su.addBranchGraph(bg);
	}

	private BranchGroup createSceneGraph() {
		BranchGroup root = new BranchGroup();

		TransformGroup spin = new TransformGroup();
		spin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		root.addChild(spin);

		Appearance ap = createTextureAppearance();
		ap.setMaterial(new Material());
		PolygonAttributes pa = new PolygonAttributes();
		pa.setBackFaceNormalFlip(true);
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		ap.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_BACK, 0));
		Shape3D shape = new Shape3D();
		shape.setGeometry(mobius().getGeometryArray());

		Transform3D tr = new Transform3D();
		tr.rotY(Math.PI);
		tr.rotX(Math.PI);
		tr.setScale(0.5);
		// tr.setTranslation(new Vector3f(0.0f, 0.0f, -10.0f));
		TransformGroup tg = new TransformGroup(tr);
		tg.addChild(shape);
		spin.addChild(tg);
		shape.setAppearance(ap);
		MouseRotate rotator = new MouseRotate(spin);
		BoundingSphere bounds = new BoundingSphere();
		rotator.setSchedulingBounds(bounds);
		spin.addChild(rotator);

		MouseTranslate translator = new MouseTranslate(spin);
		translator.setSchedulingBounds(bounds);
		spin.addChild(translator);

		MouseWheelZoom zoom = new MouseWheelZoom(spin);
		zoom.setSchedulingBounds(bounds);
		spin.addChild(zoom);

		Background background = new Background(1.0f, 1.0f, 1.0f);
		background.setApplicationBounds(bounds);
		root.addChild(background);

		AmbientLight light = new AmbientLight(true, new Color3f(Color.BLACK));
		light.setInfluencingBounds(bounds);
		root.addChild(light);

		PointLight ptlight = new PointLight(new Color3f(Color.white), new Point3f(0.5f, 0.5f, 1f),
				new Point3f(1f, 0.2f, 0f));
		ptlight.setInfluencingBounds(bounds);
		root.addChild(ptlight);

		return root;
	}

	private GeometryInfo mobius() {

		int rows = 500;
		int cols = 500;
		int p = 4 * ((rows - 1) * (cols - 1));

		IndexedQuadArray iqa = new IndexedQuadArray(rows * cols,
				GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2, p);
		Point3d[] vertices = new Point3d[rows * cols];
		int index = 0;

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				double u = i * (4 * (Math.PI)) / (rows - 1);
				double v = -0.3 + (j * (0.6 / (cols - 1)));
				double x = (1 + v * Math.cos(u / 2)) * Math.cos(u);
				double y = (1 + v * Math.cos(u / 2)) * Math.sin(u);
				double z = v * Math.sin(u / 2);
				vertices[index] = new Point3d(x, y, z);
				index++;
			}
		}

		index = 0;

		TexCoord2f[] tex = new TexCoord2f[rows * cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				tex[index] = new TexCoord2f();
				// tex[index] = new TexCoord2f(i * 1f / rows, j * 1f / cols);
				tex[index] = new TexCoord2f((rows - 1 - i) * 1f / rows, j * 1f / cols);
				// tex[index] = new TexCoord2f(i * 1f / rows, (cols-1-j) * 1f / cols);
				index++;
			}
		}

		iqa.setCoordinates(0, vertices);
		iqa.setTextureCoordinates(0, 0, tex);
		int[] texIndices = new int[p];
		index = 0;

		for (int i = 0; i < rows - 1; i++) {
			for (int j = 0; j < cols - 1; j++) {

				iqa.setCoordinateIndex(index, i * rows + j);
				texIndices[index] = i * rows + j;
				index++;
				iqa.setCoordinateIndex(index, i * rows + j + 1);
				texIndices[index] = i * rows + j + 1;
				index++;
				iqa.setCoordinateIndex(index, (i + 1) * rows + j + 1);
				texIndices[index] = (i + 1) * rows + j + 1;
				index++;
				iqa.setCoordinateIndex(index, (i + 1) * rows + j);
				texIndices[index] = (i + 1) * rows + j;
				index++;
			}
		}

		iqa.setTextureCoordinateIndices(0, 0, texIndices);
		GeometryInfo gi = new GeometryInfo(iqa);
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(gi);
		return gi;
	}

	Appearance createTextureAppearance() {
		Appearance ap = new Appearance();
		BufferedImage bi = new BufferedImage(1024, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) bi.getGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, 1024, 128);
		g2.setFont(new Font("Serif", Font.BOLD, 36));
		g2.setColor(new Color(200, 0, 0));
		g2.drawString("Mobius Strip", 0, 100);
		ImageComponent2D image = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, bi);
		Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, image.getWidth(), image.getHeight());
		texture.setImage(0, image);
		texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
		ap.setTexture(texture);
		TextureAttributes textatt = new TextureAttributes();
		textatt.setTextureMode(TextureAttributes.COMBINE);
		ap.setTextureAttributes(textatt);
		ap.setMaterial(new Material());
		return ap;

	}
}
