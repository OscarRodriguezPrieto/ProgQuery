package drawable.polygons;

import java.awt.geom.Point2D;

import drawable.Figure2D;

public class Polygon implements Figure2D {

	final Point2D[] points;//line 9

	public Polygon(Point2D... pts) {//line 11
		if (pts.length < 3)//line 12
			throw new IllegalArgumentException("A polygon must have at least three vertices.");
		clonePoints(pts, this.points = new Point2D[pts.length]);//line 14
	}

	private static void clonePoints(Point2D[] src, Point2D[] dest) {
		if (src.length != dest.length)
			throw new IllegalArgumentException("Point arrays must have the same length.");
		for (int i = 0; i < src.length; i++)
			dest[i] = (Point2D) src[i].clone();
	}

	public void setPoint(int index, Point2D newPoint) {
		points[index] = newPoint;
	}

	@Override
	public double getPerimeter() {
		double perimeter = 0;
		int nVertices = points.length;
		for (int i = 0; i < nVertices; i++)
			perimeter += points[i].distance(points[(i + 1) % nVertices]);
		return perimeter;
	}

}
