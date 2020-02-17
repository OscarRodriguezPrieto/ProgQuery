package drawable.polygons;

import java.awt.geom.Point2D;

public class RegularPolygon extends Polygon {

	public RegularPolygon(Point2D... points) {
		super(points);
		if (!equidistantPoints(points))
			throw new IllegalArgumentException("These points do not represent a regular polygon.");
	}

	private boolean equidistantPoints(Point2D[] points) {
		double distance = points[points.length - 1].distance(points[0]);
		for (int i = 0; i < points.length - 1; i++)
			if (points[i].distance(points[i + 1]) != distance)
				return false;
		return true;
	}

	@Override
	public double getPerimeter() {
		return points[0].distance(points[1]) * points.length;
	}

	@Override
	public void setPoint(int index, Point2D newPoint) {
		throw new IllegalStateException("You cannot modify points in a regular polygon.");
	}
}
