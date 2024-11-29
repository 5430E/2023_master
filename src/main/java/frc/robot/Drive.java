package frc.robot;

//import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;
//import edu.wpi.first.wpilibj.SpeedController;
//import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import frc.robot.C.Drivestyle;

import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
//imports functions from java libaries
import com.ctre.phoenix.motorcontrol.TalonFXInvertType;
import com.ctre.phoenix.motorcontrol.can.TalonFXConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonFX;

public class Drive implements Runnable{

/*Declares motor ports.
    2 = backRight (FX)
    3 = backLeft (FX)
    4 = frontRight (FX)
    5 = frontLeft (FX)
    6 = seatMotors (SRX)
    7 = gripMotors (SRX)
    8 = extendMotor (SRX)
    9 = Spare (SRX?)
  */
    public static int[] motorPorts = { 0, 1, 2, 3, 4, 5, 6, 7, 8,9,10,11 };
    static boolean loop = true; //enables loop for teleop
    static Timer _timer = new Timer(); //declares a timer for the smooth in/out system
    //declares motorcontrollers
    static WPI_TalonFX backRightMotor = new WPI_TalonFX(motorPorts[2]);
    static WPI_TalonFX backLeftMotor = new WPI_TalonFX(motorPorts[3]);
    static WPI_TalonFX frontRightMotor = new WPI_TalonFX(motorPorts[4]);
    static WPI_TalonFX frontLeftMotor = new WPI_TalonFX(motorPorts[5]);
    //organizes motor conrollers into groups, left and right respectively
    static MotorControllerGroup leftGroup = new MotorControllerGroup(backLeftMotor, frontLeftMotor);
    static MotorControllerGroup rightGroup = new MotorControllerGroup(backRightMotor, frontRightMotor);
    //creates a differential (tank) drive out of the two motor controller groups
    public static DifferentialDrive driveTrain = new DifferentialDrive(leftGroup, rightGroup);
    static TalonFXConfiguration configs = new TalonFXConfiguration();
    final TalonFXInvertType left = TalonFXInvertType.Clockwise;
    final static TalonFXInvertType right = TalonFXInvertType.CounterClockwise;
    


    static boolean auton = false;
    final static double kRadius = 3;
    final static double kCircumference = (Math.PI) * 2 * kRadius;
    final int units = 2048;
    public static double power1 = 0;
    public static double power2 = 0;
    public boolean isChezy = false;
 
//New constructors 
    public DriveStyle m_drivestyle = new DriveStyle();

    public Drive() {
      SupplyCurrentLimitConfiguration configTalonCurrent = new SupplyCurrentLimitConfiguration(true,55,0,0);
      backRightMotor.configSupplyCurrentLimit(configTalonCurrent);
      frontLeftMotor.configSupplyCurrentLimit(configTalonCurrent);
      backLeftMotor.configSupplyCurrentLimit(configTalonCurrent);
      backLeftMotor.configSupplyCurrentLimit(configTalonCurrent);
    }

    public void run() { 

      //frontRightMotor.setInverted(true);
      //while teleop is looping
      while (loop) {
     
        if (!auton) { //if not auton
  
//          if(Robot.joystickRight.getRawButton(2)){
//            this.setChezy(true);
//          } 
          if(Robot.joystickLeft.getRawButton(2)){
            this.setChezy(false);
          }
          
          if (isChezy) {
          //call chezy here
//          m_drivestyle.cheesyDrive(Robot.joystickLeft.getRawAxis(1),Robot.joystickRight.getRawAxis(2)); //Throttle on left vertical (1), wheel on right horizontal (2)
          //the drivestyle subclass does its work, then we pull the values it just generated into the driveTrain to set leftright 
//          driveTrain.tankDrive(m_drivestyle.getLeftPower() * VariableSpeed.getMultiplier(),m_drivestyle.getRightPower() * VariableSpeed.getMultiplier());
          }

          else { //call original 5430 drive code




            if ((Robot.joystickLeft.getRawAxis(1) > .02) || Robot.joystickLeft.getRawAxis(1) < -.02 //if there is input over 0.02 on the left joystick
              || Robot.joystickRight.getRawAxis(1) > .02 || Robot.joystickRight.getRawAxis(1) < -.02) { //or the right stick
                 _timer.start(); //start the timer
                if(_timer.get() > .5){ //if the timer is greater than .5 seconds
                    _timer.stop(); //stop the timer
                       }          
             driveTrain.tankDrive(-(Robot.joystickLeft.getRawAxis(1)  * VariableSpeed.getMultiplier() /*_timer.get()*2  */ ) ,(Robot.joystickRight.getRawAxis(1) * VariableSpeed.getMultiplier() * 1.05 /*_timer.get()*2 */ )); //drive train takes inputs from joystick and multiplies by two as well as the timer, creating a smoothing system
            } else { //if there is not input
            driveTrain.tankDrive(0, 0); //set both sides to 0
            _timer.reset(); //reset the smoothing timer
            // System.out.println("check"); */
            }
          }

        } else { //In Auto, set the drivetrain to 0 power
          driveTrain.tankDrive(power1, power2);
          // System.out.println("check");
        }
      
      
    
        }
    }
    
    //Subclasses for Chezy drive
    //This function lets us make a button binding elsewhere to turn on or off the halo drive 
    public void setChezy(boolean isChezy) {
        this.isChezy = isChezy;
    }
}
