
public class Value
{
    public static Value VOID = new Value(new Object());

    final Object value;

    public Value(Object value)
    {
        this.value = value;
    }

    public boolean asBoolean()
    {
        return (boolean)value;
    }

    public String asString()
    {
        return (String)value;
    }

    public Double asFloatingPoint()
    {
        if (value instanceof Integer)
            return ((Integer)value).doubleValue();
        return (Double)value;
    }

    public boolean isDouble()
    {
        return value instanceof Double;
    }

    public int asInteger() {
        Integer a;
        return (Integer)value;
    }

    public boolean isInteger() {
        return value instanceof Integer;
    }

    public Object asObject() {
        return value;
    }

    @Override
    public boolean equals(Object o) {

        if(value == o) {
            return true;
        }

        if(value == null || o == null || o.getClass() != this.getClass()) {
            return false;
        }

        Value that = (Value)o;

        return this.value.equals(that.value);
    }

    @Override
    public String toString()
    {
        return String.valueOf(value);
    }

}
