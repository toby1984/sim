package de.codesourcery.sim.entitymanager;

import de.codesourcery.sim.Vec2D;
import de.codesourcery.sim.taskplanning.TaskSource;

public class Building implements TaskSource, IEntity
{
    public final int id;
    public final Vec2D location;
    public final BuildingType type;

    public Building(int id, BuildingType type, Vec2D location)
    {
        this.id = id;
        this.location = new Vec2D(location);
        this.type = type;
    }

    public BuildingType getBuildingType()
    {
        return type;
    }

    public boolean hasBuildingType(BuildingType t) {
        return t == this.type;
    }

    @Override
    public int getID()
    {
        return id;
    }

    @Override
    public Vec2D getLocation()
    {
        return location;
    }

    @Override
    public Type getType()
    {
        return Type.BUILDING;
    }
}