package database.querys.cypherWrapper;

import database.relations.RelationTypesInterface;
import utils.dataTransferClasses.Pair;

public class EdgeImpl implements Edge {

	private EdgeDirection direction;
	private String name;
	private RelationTypesInterface[] relLabel;
	private Cardinalidad cardinalidad;
	private Pair<String, Object>[] property = new Pair[0];

	public EdgeImpl(EdgeDirection direction, String name, Cardinalidad cardinalidad,
			RelationTypesInterface... relLabel) {
		this.direction = direction;
		this.name = name;
		this.relLabel = relLabel;
		this.cardinalidad = cardinalidad;
	}

	public EdgeImpl(String name, Cardinalidad cardinalidad, RelationTypesInterface... relLabel) {
		this(EdgeDirection.INCOMING, name, cardinalidad, relLabel);
	}

	public EdgeImpl() {
		this(EdgeDirection.INCOMING, "", Cardinalidad.JUST_ONE);
	}

	public EdgeImpl(String name) {
		this(EdgeDirection.INCOMING, name, Cardinalidad.JUST_ONE);
	}

	public EdgeImpl(EdgeDirection direction) {
		this(direction, "", Cardinalidad.JUST_ONE);
	}

	public EdgeImpl(EdgeDirection direction, RelationTypesInterface... relLabel) {
		this(direction, "", Cardinalidad.JUST_ONE, relLabel);
	}

	public EdgeImpl(RelationTypesInterface... relLabel) {
		this(EdgeDirection.INCOMING, "", Cardinalidad.JUST_ONE, relLabel);
	}

	public EdgeImpl(String name, RelationTypesInterface... relLabel) {
		this(EdgeDirection.INCOMING, name, Cardinalidad.JUST_ONE, relLabel);
	}

	public EdgeImpl(Cardinalidad c, RelationTypesInterface... relLabel) {
		this(EdgeDirection.INCOMING, "", c, relLabel);
	}

	public EdgeImpl setDirection(EdgeDirection direction) {
		this.direction = direction;
		return this;
	}

	public EdgeImpl setName(String name) {
		this.name = name;
		return this;
	}

	public EdgeImpl setRelLabel(RelationTypesInterface[] relLabel) {
		this.relLabel = relLabel;
		return this;
	}

	public EdgeImpl setCardinalidad(Cardinalidad cardinalidad) {
		this.cardinalidad = cardinalidad;
		return this;
	}

	public EdgeImpl setProperty(Pair<String, Object>... property) {
		this.property = property;
		return this;
	}

	@Override
	public String edgeToString() {
		return direction.getDirectionToStringPart1() + name + labelsToString() + propertiesToString()
				+ cardinalidad.toString() + direction.getDirectionToStringPart2();
	}

	private String labelsToString() {
		if (relLabel.length == 0)
			return "";
		String res = ":";
		for (RelationTypesInterface r : relLabel)
			res += r.toString() + "|";
		return res.substring(0, res.length() - 1);
	}

	private String propertiesToString() {
		if (property.length == 0)
			return "";
		String res = "{";
		for (Pair p : property)
			res += p.getFirst().toString() + "=" + (p.getSecond() instanceof String
					? "'" + p.getSecond().toString() + "'" : p.getSecond().toString()) + ",";
		return res.substring(0, res.length() - 1) + "}";
	}

	public EdgeImpl changeDirection() {
		direction = direction == EdgeDirection.UNDIRECTED ? direction
				: direction == EdgeDirection.INCOMING ? EdgeDirection.OUTGOING : EdgeDirection.INCOMING;
		return this;
	}
}
