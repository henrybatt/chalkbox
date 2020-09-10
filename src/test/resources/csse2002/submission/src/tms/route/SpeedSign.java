package tms.route;

public class SpeedSign {
    /**
     *  Creates an object SpeedSign which is an LED speed sign that can be changed
     */
    private int speed;
    public SpeedSign(int initialSpeed){
        /**
         * Creates a new electronic speed sign with the given initial displayed speed.
         * @param initialSpeed the initial speed limit to be shown on the sign
         */
        speed = initialSpeed;
    }
    public int getCurrentSpeed(){
        /**
         * Get the speed displayed by the sign, not the default speed for the route
         * @return the current speed limit displayed by this sign
         */
        return speed;
    }
    public void setCurrentSpeed(int speed){
        /**
         * Changes the displayed speed
         * @param speed the new speed to display
         */
        this.speed = speed;
    }
}
