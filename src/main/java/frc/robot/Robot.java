// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.phoenix.motorcontrol.ControlMode;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  // Threads
  Drive driveRef = new Drive();
  public Thread drive = new Thread(driveRef); // creates a thread for drivetrain

  Arm armRef = new Arm();
  public Thread arm = new Thread(armRef);
 // Camera cameraRef = new Camera();
 // public Thread camera = new Thread(cameraRef);
  Extend extendRef = new Extend();
  public Thread extend = new Thread(extendRef);
  Gripper gripperRef = new Gripper();
  public Thread gripper = new Thread(gripperRef);

  // JL, Declares autos as strings
  private static final String kDefaultAuto = "Default";
  private static final String kUTurnAuto = "Uturn Auto";
  private static final String kLoopAuto = "Loop Auto";
  private static final String kBluDockOnly = "Blue Dock Only";
  private static final String middleauto = "Middle Auto Position";
  private static final String shootdock = "Shoot and Dock Autonomous Option";
  private static final String kBluSubstation = "Blue Substation";
  private static final String kDriveInMultipleTest = "DriveInMultipleTest";
 // private static final String kBlueSubstationCone = "Blue Substation Cone";
  private static final String kDirectionalChecks = "Directional Checks";
  private static final String kAdvancedRingAround = "Ring Around";
  private static final String kstayStill = "stay still";
  //private static final String k

  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  public double speedConstant = 3.22;
  public double maxSpeed = 16.4;

  public int autoStatus = 0;
  

  // joysticks
  public static Joystick joystickLeft = new Joystick(1);
  public static Joystick joystickRight = new Joystick(2);
  public static Joystick controller0 = new Joystick(0);

  NetworkTableInstance inst = NetworkTableInstance.getDefault();
  NetworkTable table = inst.getTable("datatable");
 
  public static Timer s_timer = new Timer();

  

  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */

  // Custom Jio code~ Essentially calculates a distance into travel for x time
  // command.
  // Distance is distance in feet, while multiple is a multiplier for the speed of
  // the robot.
  // 1 is regular, 2 is 2x, 0.5 would be half, and so on. Original speed is about
  // 3.22, but the constant is changable
  // previously in the code. Larger multiple value means faster travel. Max
  // multiple should be 5. DO NOT GO PAST 5.

   
  public void driveInMultiple(double distance, double multiple) {
    // *** driveInMultiple currwntly not working *TESTED*
      s_timer.restart();
    System.out.println("Attempting to drive in multiple: " + distance + "," + multiple);
    while (s_timer.get() < (distance / (speedConstant * multiple))) {
      Drive.driveTrain.tankDrive(0.2 * multiple, -0.2 * multiple);
    }
    Drive.driveTrain.tankDrive(0, 0);
  }
  

  // Custom Jio code~ Essentially caluculates a velocity to travel using a
  // distance and time given.
  // Distance is distance in feet, time is the time in seconds provided to make
  // the trip.
  // Smaller time = Faster velocity
  public void driveInPower(double power, double time) {
    // ***  Drive in power is working *TESTED*
      s_timer.restart();
    while (s_timer.get() < time) {
      Drive.driveTrain.tankDrive(power, -power);
    }
    Drive.driveTrain.tankDrive(0, 0);
  }

  public void driveInTime(double distance, double time) {
      s_timer.restart();
    while (s_timer.get() < time) {
      Drive.driveTrain.tankDrive((distance / time) / maxSpeed, -(distance / time) / maxSpeed);
    }
    Drive.driveTrain.tankDrive(0, 0);
  }

  // Custom Jio code~ Temporary placeholder function for 90 degree turns.

  public void turn90Degrees(String direction) {
      s_timer.restart();
    while (s_timer.get() < 1) {
      
      switch (direction) {
        case "left":
          Drive.driveTrain.tankDrive(-0.50, -0.50);
          break;
        case "right":
          Drive.driveTrain.tankDrive(0.45, 0.45);
          break;
      }
    }
    Drive.driveTrain.tankDrive(0, 0);
  }

  /* JL, Documentation on the doc. To adress the issue about how the same autom code will not work for both 
  alliances (for reference, a code starting off flush with grid, turning left, and driving forward will 
  result in the robot being either closer (Blue) or farther (Red) from the loading zone depending on the 
  alliance the robot is on), I created a new function called turn90DegreesAdvanced, which will literally 
  take the alliance it is on when the DS is plugged in and make the turn properly match the corresponding 
  autom. The ref point should always be Blue Alliance, as all of our auto plans are based off of starting 
  on Blue. 
 */

  public void turn90DegreesAdvanced(String direction) {
    s_timer.restart();
    while (s_timer.get() < .5) {
      
      switch (direction) {
        case "left":
        if(DriverStation.getAlliance() == Alliance.Blue){
          Drive.driveTrain.tankDrive(-0.6, -0.6);
          break;
        }else{
          Drive.driveTrain.tankDrive(0.6, 0.6);
          break;
        }
        case "right":
        if(DriverStation.getAlliance() == Alliance.Blue){
          Drive.driveTrain.tankDrive(0.6, 0.6);
          break;
        }else{
          Drive.driveTrain.tankDrive(-0.6, -0.6);
          break;
        }
      }
    }
    Drive.driveTrain.tankDrive(0, 0);
  }

  public void turnArm(double time, double speed){
    s_timer.restart();
    while (s_timer.get() < time){
      Arm.seatMotors.set(ControlMode.PercentOutput, speed);
    }
    Arm.seatMotors.set(ControlMode.PercentOutput, 0);
  }
 
   
  @Override
  public void robotInit() {
    // Thread starters
    drive.start();
    arm.start();
 //   camera.start();
    gripper.start();
    extend.start();

    // Puts auto list onto Shuffleboard
    SmartDashboard.putData("Auton Choice", m_chooser);
    
    m_chooser.addOption("Loop Auto", kLoopAuto);
   // m_chooser.addOption("Volt Switch", kUTurnAuto);
    m_chooser.addOption("Middle Position Auto", middleauto);
    m_chooser.addOption("Shoot and Dock Autonomous Option", shootdock);
    m_chooser.addOption("Stay Still", kstayStill);
   // m_chooser.addOption("Blue Dock Only", kBluDockOnly);
    m_chooser.addOption("Default", kDefaultAuto);
   // m_chooser.addOption("Blue Substation", kBluSubstation);
   // m_chooser.addOption("test", kDriveInMultipleTest);
   // m_chooser.addOption("Directional Checks", kDirectionalChecks);
   // m_chooser.addOption("Advanced Ring Around", kAdvancedRingAround);

  }

  @Override
  public void robotPeriodic() {

    // updating the value from the encoder
    SmartDashboard.putNumber("Seat motor Values", Arm.position);
    SmartDashboard.putNumber("Multiplier", Drive.getMultiplier());
    SmartDashboard.putNumber("Encoder Value", Drive.getEncoderValue());

  }

  @Override
  public void autonomousInit() {

    m_autoSelected = m_chooser.getSelected();
    autoStatus = 0;
    switch (m_autoSelected){
      case middleauto:
        System.out.println("driving in powah");
        driveInPower(0.5, 3.0);
        break;
    }
  }

  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kDefaultAuto:
    if (autoStatus == 0){
        System.out.println("Attempting to use Default Auto");
          s_timer.restart();
        while(s_timer.get() < 2.5){
          Arm.seatMotors.set(ControlMode.PercentOutput, -0.9);
        }
          System.out.println("Working... 1");
        Extend.armExtend(0.3);
          System.out.println("yummy... 2");

        // Gripper.gripperBite(0.3);//extend the arm with 30% power but since we dont
        // know yet, well just leave this as a prototype
        /**
         * Ethan here, im proposing if we get the length of the arm by getting distance
         * the robot has traveled
         * minus 18.666667 (which is the length of the nodes to the game piece but since
         * we are using percentage to extend the arm, we dont know yet.)
         *
         *
         **/
        Extend.armRetract(0.35);
        s_timer.restart();
        while(s_timer.get() < 2.5){
        Arm.seatMotors.set(ControlMode.PercentOutput, 0.9);
      }
          System.out.println("Attempted to Retract arm...3");
        for (int twice = 0; twice < 2; twice++) {
          System.out.println("For loop occuring:" + twice); // this one is supposed to be here
          turn90Degrees("left"); // turn 180 degrees
        } // changeable depending on how far we want the robot should be from the target
        driveInPower(.5, 2);
         System.out.println("Drive in multiple issued...6");
        Extend.armExtend(0.5);
          System.out.println("Arm extend issued...7");
        // insert rotate arm length
        // Gripper.gripperBite(0.0); // opening the gripper by setting power of motor to
        // zero
        System.out.println("I think I'm done now!"); 
        autoStatus = 1;

      }
        break;

      case kUTurnAuto:

        System.out.println("EMOLGA, use Volt Switch! It doesn't affect the opposing BOLDORE!");

        break;
      case kLoopAuto: 
        if (autoStatus == 0) {
          
            s_timer.restart();
          while (s_timer.get() < 3) { // program runs for 2 sec
            Arm.seatMotors.set(ControlMode.PercentOutput, 0.9); // arm will go down
          }
          System.out.println("Working... 1");
          Extend.armExtend(0.5);
           System.out.println("Retracting... 1");
          Gripper.gripperRetract();
           s_timer.restart();
          while (s_timer.get() < 3) { // program runs for 2 sec
            Arm.seatMotors.set(ControlMode.PercentOutput, -0.9); // arm will go down
          }
          System.out.println("LLLL"); //Ethan was here when he stopped.
          for (int twice = 0; twice < 2; twice++) {
            System.out.println("Attempting to turn 180:" + twice);
            turn90Degrees("left"); // turn 180 degrees
          }
          //Loop done
          System.out.println("About to drive in power...7");
          driveInPower(.5, 5.0);
            System.out.println("Drove in power...4");
            autoStatus = 1;


        }

      break;
     // case middleauto:
       // System.out.println("Attempting middleauto");
        /*if (autoStatus == 0) {
              System.out.println("driving in powah");
          driveInPower(0.5, 3.0);
          autoStatus = 1;
        }*/

     //   break; 
     
      case shootdock:
        System.out.println("Going to attempt shootdock");
        if (autoStatus == 0) {
          Gripper.gripperBite("cube");
           s_timer.restart();
            while(s_timer.get() < 3){
              Arm.seatMotors.set(ControlMode.PercentOutput, -0.9);
            }
             System.out.println("About to extend...1");
          Extend.armExtend(0.75);
         s_timer.restart();
          while(s_timer.get() < 1.2){
            Gripper.gripperMotor.set(ControlMode.PercentOutput, -0.7);
          }
           s_timer.restart();
          while(s_timer.get() < 1){
            Gripper.gripperMotor.set(ControlMode.PercentOutput, 0.7);
          }
          if (autoStatus == 0) {
              System.out.println("eating food!!...3");
            Extend.armRetract(0.53);
            s_timer.restart();
            while(s_timer.get() < 3){
              Arm.seatMotors.set(ControlMode.PercentOutput, 0.81);
            }
               System.out.println("Going to drive in power...6");
            driveInPower(0.8, 2.5);
            Drive.driveTrain.tankDrive(0.0, 0.0);
               System.out.println("Whomst've'dk'tve'ya'wro'rea'fga?");
               autoStatus = 1;

          }
        }
      break;

      case kstayStill:
      break; 

      case kBluDockOnly:
        System.out.println("Attempting to BluDockOnly");
        if (autoStatus == 0) {
          autoStatus = 1;
          System.out.println("Going to let go...1");
          Gripper.gripperRetract();
          System.out.println("Going to drive 2.5 feet backwards...2");
          driveInPower(.2, 1);
          System.out.println("Going to turn right...3");
          turn90Degrees("right");
          System.out.println("Going to drive 2.5 feet forwards...4");
          driveInPower(.3, 2);
          System.out.println("Going to turn right...5");
          turn90Degrees("right");
          System.out.println("Going to drive 2 feet forwards...6");
          driveInPower(.2, 5);
          System.out.println("Hooray! I finished!");
        }
      break;

      case kBluSubstation:
      System.out.println("Attempting to BlueSubStation");
        if (autoStatus == 0){
          Gripper.gripperBite("cone");
          System.out.println("Going to let go...1");
          Gripper.gripperRetract();
          System.out.println("Going to drive 2.5 feet backwards...2");
          driveInPower(0.5, 1);
          System.out.println("Going to turn right...3");
          turn90Degrees("right");
          System.out.println("Going to turn right again...4");
          turn90Degrees("right");
          System.out.println("Going to drive in muliple...5");
          driveInPower(0.1, 5);
          System.out.println("Peanut Butter Jelly Sandwiches!");
          autoStatus = 1;

        }
      break;

      

      case kDriveInMultipleTest:
      if(autoStatus == 0){
        autoStatus = 1;
        System.out.println("Attempting to drive in multiple.");
        driveInMultiple(10, 2);
        System.out.println("Attempting to stop.");
        Drive.driveTrain.tankDrive(0, 0);
        System.out.println("Done"); 
      }
      break;

      case kDirectionalChecks:
      if(autoStatus == 0){
        autoStatus = 1;
        System.out.println("Directional checks!");
        System.out.println("Turning left! (-0.6, -0.6)");
        turn90Degrees("left"); 
        System.out.println("Turning right! (0.6, 0.6)");
        turn90Degrees("right");
        System.out.println("Driving forward! (+, -)");
        driveInPower(0.4, 1);
        System.out.println("Driving backward! (-, +)");
        driveInPower(-0.4, 1);
        System.out.println("Donesies! Order should have been: Left, Right, Forward, Backward.");
      
      }
      break;

      case kAdvancedRingAround:

      System.out.println("Attempting Advanced Ring Around");
      if(autoStatus == 0){
        autoStatus = 1;
        Gripper.gripperBite("cube");
        //driveInPower(0, 2);
        turnArm(2, 0.8);
        Gripper.gripperRetract();
        Extend.armRetract(0.35);
        turnArm(4, 0.8);
        driveInMultiple(15, 3);
        Gripper.gripperBite("cube");
        turn90DegreesAdvanced("left");
        driveInMultiple(7, 3);
        turn90DegreesAdvanced("left");
        driveInMultiple(10, 4);
      }

      break;


    }
  }

  @Override
  public void teleopInit() {
    Drive.driveTrain.tankDrive(0, 0);
  }

  @Override
  public void teleopPeriodic() {
  }

  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
  }

  @Override
  public void testInit() {
  }

  @Override
  public void testPeriodic() {
  }

  @Override
  public void simulationInit() {
  }

  @Override
  public void simulationPeriodic() {
  }

  public static double joystickVal(int port) {
    return controller0.getRawAxis(port);
  }

  public static boolean joystickButton(int button) {
    return controller0.getRawButton(button);
  }

  }
