// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
// import edu.wpi.first.wpilibj2.command.button.CommandGenericHID;

/** Add your docs here. */
public class DriveStyle {//These variables are used in ChezyDrive, the Halo-type (similar to Arcade) drive we borrow from team 254 (Uses an additional control term that compensates for robot momentum)
    private boolean isHighGear = false; //haven't used highgear for a few years, but no reason to delete
    private double oldWheel = 0.0; //accumulator to handle inertia
    private double quickStopAccumulator = 0; 
    private boolean isQuickTurn = false;
    // private boolean slowMode = false; 
    private double leftpower = 0;
    private double rightpower = 0;


    public void setQuickTurn() {
            isQuickTurn = true;
        }
    public void resetQuickTurn() {
            isQuickTurn = false;
        }
     /**
         * This method does the Halo drive and is the slim down version of the cheesy
         * poofs drive style. 
         * QuickTurn is handled inside this method.
         * It has the capability to auto shift if uncommented.
         * Without shifting, the Low Gear values & if() cases are used everywhere
         * 
         * @param stick
         */
    public void cheesyDrive(double wheelInput, double throttleInput) {
    
    
            double wheelNonLinearity;
            double wheel = handleDeadband(wheelInput, C.Drivestyle.wheelDeadband); // double
                                                                             // wheel
                                                                             // =
                                                                             // handleDeadband(controlBoard.rightStick.getX(),
                                                                             // wheelDeadband);
            double throttle = -handleDeadband(throttleInput, C.Drivestyle.throttleDeadband);
            double negInertia = wheel - oldWheel;
            /*
             * if(getAverageSpeed()> 2000){ SetHighGear(); } else if (getAverageSpeed() <
             * 1500){ SetLowGear(); } //Autoshifting code based on a speed threshold from encoder data (not implemented)
             */
    
            oldWheel = wheel;
            if (isHighGear) {
                wheelNonLinearity = C.Drivestyle.wheelNonLinearityHighGear;
                // Apply a sin function that's scaled to make it feel better. WPILib does similar thing by squaring inputs.
                // Yes, this is repeated 2x on purpose, but only as a "matter of taste" based on driver feedback circa 2014
                wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
                wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
            } else {
                wheelNonLinearity = C.Drivestyle.wheelNonLinearityLowGear;
                // Apply a sin function that's scaled to make it feel better. WPILib does a similar thing by squaring inputs.
                // Yes, this is repeated 3 times on purpose, but only as a "matter of taste" based on driver feedback circa 2014
                wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
                wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
                wheel = Math.sin(Math.PI / 2.0 * wheelNonLinearity * wheel) / Math.sin(Math.PI / 2.0 * wheelNonLinearity);
            }
    
            double leftPwm, rightPwm, overPower;
            double sensitivity;
            double angularPower;
            double linearPower;
            // Negative inertia!
            double negInertiaAccumulator = 0.0;
            double negInertiaScalar;
    
            if (isHighGear) {
                negInertiaScalar = 5.0;
                sensitivity = C.Drivestyle.sensitivityHigh; // sensitivity =
                                                 // C.sensitivityHigh.getDouble();
            } else {
                if (wheel * negInertia > 0) {
                    negInertiaScalar = 2.5;
                } else {
                    if (Math.abs(wheel) > 0.65) {
                        negInertiaScalar = 5.0;
                    } else {
                        negInertiaScalar = 3.0;
                    }
                }
                sensitivity = C.Drivestyle.sensitivityLow; // sensitivity =
                                                // C.sensitivityLow.getDouble();
                if (Math.abs(throttle) > 0.1) {
                    // sensitivity = 1.0 - (1.0 - sensitivity) / Math.abs(throttle);
                }
            }
    
            double negInertiaPower = negInertia * negInertiaScalar;
            negInertiaAccumulator += negInertiaPower;
            wheel = wheel + negInertiaAccumulator;
            if (negInertiaAccumulator > 1) {
                negInertiaAccumulator -= 1;
            } else if (negInertiaAccumulator < -1) {
                negInertiaAccumulator += 1;
            } else {
                negInertiaAccumulator = 0;
            }
            linearPower = throttle;
            // Quickturn!
            if (isQuickTurn) {
                if (Math.abs(linearPower) < 0.2) {
                    double alpha = 0.1;
                    quickStopAccumulator = (1 - alpha) * quickStopAccumulator + alpha * limit(wheel, 1.0) * 5;
                }
                overPower = 1.0;
                if (isHighGear) {
                    sensitivity = C.Drivestyle.QuickTurnSensitivityHigh; 
                } else {
                    sensitivity = C.Drivestyle.QuickTurnSensitivityLow;
    
                }
                angularPower = wheel;
            } else {
                overPower = 0.0;
                angularPower = Math.abs(throttle) * wheel * sensitivity - quickStopAccumulator;
                if (quickStopAccumulator > 1) {
                    quickStopAccumulator -= 1;
                } else if (quickStopAccumulator < -1) {
                    quickStopAccumulator += 1;
                } else {
                    quickStopAccumulator = 0.0;
                }
            }
            rightPwm = leftPwm = linearPower;
            leftPwm += angularPower; //Flipped in 2020 for flipped gearboxes, reverted for 5430 standard drive
            rightPwm -= angularPower; //Flipped in 2020 for flipped gearboxes, reverted for 5430 standard drive
            if (leftPwm > 1.0) {
                rightPwm -= overPower * (leftPwm - 1.0);
                leftPwm = 1.0;
            } else if (rightPwm > 1.0) {
                leftPwm -= overPower * (rightPwm - 1.0);
                rightPwm = 1.0;
            } else if (leftPwm < -1.0) {
                rightPwm += overPower * (-1.0 - leftPwm);
                leftPwm = -1.0;
            } else if (rightPwm < -1.0) {
                leftPwm += overPower * (-1.0 - rightPwm);
                rightPwm = -1.0;
            }
           // SetLeftRight(leftPwm, -rightPwm);
            leftpower = leftPwm;
            rightpower = -rightPwm;
        }
    
    
        /**
         * This method takes in the object joystick and returns the y axis value to the
         * left most side of the gamepad.
         * 
         * @param stick
         * @return yAxis
         */
        public double getYAxisLeftSide(Joystick stick) {
            return stick.getY();
        }
    
        /**
         * This method takes inthe ojbect joystick and returns the y axis value to the
         * right most siide of the gamepad.
         * 
         * @param stick
         * @return
         */
        public double getYAxisRightSide(Joystick stick) {
            return stick.getThrottle();
        }
    
        /**
         * If the value is too small make it zero
         * 
         * @param val
         * @param deadband
         * @return value with deadband
         */
        public double handleDeadband(double val, double deadband) {
            return (Math.abs(val) > Math.abs(deadband)) ? val : 0.0;
        }
    

    
        /**
         * If the value is too large limit it.
         * 
         * @param v
         * @param limit
         * @return value with a max limit
         */
    
        public double limit(double v, double limit) {
            return (Math.abs(v) < limit) ? v : limit * (v < 0 ? -1 : 1);
        }
    
        
         /**
         * This method takes in object joystick and returns the yaxis value of the left
         * most side of the Logitech F310 gamepad.
         * 
         * @param stick
         * @return yAxis
         */
        public double getThrottle(Joystick stick) {
            return stick.getY();
        }
    
        /**
         * This method takes in the object joystick and returns the x axis value to the
         * right most side of the gamepad when using a Logitech F310 gamepad
         * 
         * @param stick
         * @return xAxis
         */
        public double getWheel(Joystick stick) {
            return stick.getZ();
        }
         
  
        //Tank drive style code
          public void tankDrive(Joystick stickLeft, Joystick stickRight){
          double axisNonLinearity;
  
        //Get Y axis and make a deadband 
          double leftY =  handleDeadband(getThrottle(stickLeft),0.02);
          double rightY =  handleDeadband(getThrottle(stickRight),0.02);
    
    
    if (isHighGear) {
        axisNonLinearity = 0.5;
        // Smooth the controls on Left side
        leftY = Math.sin(Math.PI / 2.0 * axisNonLinearity * leftY) /
            Math.sin(Math.PI / 2.0 * axisNonLinearity);
        leftY = Math.sin(Math.PI / 2.0 * axisNonLinearity * leftY) /
            Math.sin(Math.PI / 2.0 * axisNonLinearity);
     
        //Smooth the controls on Right side
        rightY = Math.sin(Math.PI / 2.0 * axisNonLinearity * rightY) /
            Math.sin(Math.PI / 2.0 * axisNonLinearity);
        rightY = Math.sin(Math.PI / 2.0 * axisNonLinearity * rightY) /
            Math.sin(Math.PI / 2.0 * axisNonLinearity);
        }
        else{
            axisNonLinearity = 0.5;
        // Smooth the controls on Left side
        leftY = Math.sin(Math.PI / 2.0 * axisNonLinearity * leftY) /
            Math.sin(Math.PI / 2.0 * axisNonLinearity);
        leftY = Math.sin(Math.PI / 2.0 * axisNonLinearity * leftY) /
            Math.sin(Math.PI / 2.0 * axisNonLinearity);
     
        //Smooth the controls on Right side
        rightY = Math.sin(Math.PI / 2.0 * axisNonLinearity * rightY) /
            Math.sin(Math.PI / 2.0 * axisNonLinearity);
        rightY = Math.sin(Math.PI / 2.0 * axisNonLinearity * rightY) /
            Math.sin(Math.PI / 2.0 * axisNonLinearity);
        }
    
        //set the motors
        //SetLeftRight(leftY * C.Drive.invert,rightY*C.Drive.invert); //Commented out because of DriveStyle being removed from the drivetrain subsystem and having no direct access
        leftpower = leftY; 
        rightpower = -rightY;
  }
    public double getLeftPower(){
        return leftpower;
    
    }
    public double getRightPower(){
        return rightpower;
    }
}
