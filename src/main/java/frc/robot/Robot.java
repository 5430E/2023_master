// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
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
  VariableSpeed safety = new VariableSpeed();
  Arm armRef = new Arm();
  public Thread arm = new Thread(armRef);
  private Thread speedSafety = new Thread(safety);
  Camera cameraRef = new Camera();
  public Thread camera = new Thread(cameraRef);
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
    s_timer.reset();
    s_timer.start();
    while (s_timer.get() < distance / (speedConstant) * 2) {
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
    s_timer.reset();
    s_timer.start();
    while (s_timer.get() < time) {
      Drive.driveTrain.tankDrive(power, -power);
    }
    Drive.driveTrain.tankDrive(0, 0);
  }

  public void driveInTime(double distance, double time) {
    s_timer.reset();
    s_timer.start();
    while (s_timer.get() < time) {
      Drive.driveTrain.tankDrive((distance / time) / maxSpeed, -(distance / time) / maxSpeed);
    }
    Drive.driveTrain.tankDrive(0, 0);
  }

  // Custom Jio code~ Temporary placeholder function for 90 degree turns.

  public void turn90Degrees(String direction) {
    s_timer.reset();
    s_timer.start();
    while (s_timer.get() < 1) {
      switch (direction) {
        case "left":
          Drive.driveTrain.tankDrive(-0.5, -0.5);
          break;
        case "right":
          Drive.driveTrain.tankDrive(0.5, 0.5);
          break;
      }
    }
    Drive.driveTrain.tankDrive(0, 0);
  }
 
   
  @Override
  public void robotInit() {
    // Thread starters
    drive.start();
    speedSafety.start();
    arm.start();
    camera.start();
    gripper.start();
    extend.start();

    // Puts auto list onto Shuffleboard
    SmartDashboard.putData("Auton Choice", m_chooser);
    m_chooser.addOption("Loop Auto", kLoopAuto);
    m_chooser.addOption("Volt Switch", kUTurnAuto);
    m_chooser.addOption("Middle Position Auto", middleauto);
    m_chooser.addOption("Shoot and Dock Autonomous Option", shootdock);
    m_chooser.addOption("Blue Dock Only", kBluDockOnly);

  }

  @Override
  public void robotPeriodic() {

    // updating the value from the encoder
    SmartDashboard.putNumber("Seat motor Values", Arm.position);

  }

  @Override
  public void autonomousInit() {

    m_autoSelected = m_chooser.getSelected();

  }

  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kDefaultAuto:

        System.out.println("Attempting to use Default Auto");

        driveInMultiple(18.6666667, 1.0);// driveiintime method just moves in a straight line
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
        Extend.armRetract(0.3);
          System.out.println("Attempted to Retract arm...3");
        for (int twice = 0; twice < 2; twice++) {
          System.out.println("For loop occuring:" + twice); // this one is supposed to be here
          turn90Degrees("left"); // turn 180 degrees
        } // changeable depending on how far we want the robot should be from the target
        driveInMultiple(18.6666667, 1.0);
         System.out.println("Drive in multiple issued...6");
        Extend.armExtend(0.5);
          System.out.println("Arm extend issued...7");
        // insert rotate arm length
        // Gripper.gripperBite(0.0); // opening the gripper by setting power of motor to
        // zero
        System.out.println("I think I'm done now!");
        break;

      case kUTurnAuto:

        System.out.println("EMOLGA, use Volt Switch! It doesn't affect the opposing BOLDORE!");

        break;
      case kLoopAuto: // basic autonomous
        if (autoStatus == 0) {
          autoStatus = 1;
           System.out.println("Retracting... 1");
          Gripper.gripperRetract();
          for (int twice = 0; twice < 2; twice++) {
            System.out.println("Attempting to turn 180:" + twice);
            turn90Degrees("left"); // turn 180 degrees
          }
          //Loop done
          driveInPower(0.3, 12.0);
            System.out.println("Drove in power...4");
          s_timer.reset();
          s_timer.start();
           System.out.println("About to run arm for 3 seconds...5");
          while (s_timer.get() < 3) { // program runs for 2 sec
            Arm.seatMotors.set(ControlMode.PercentOutput, -0.1); // arm will go down
          }
            System.out.println("Going to grip..6");
          Gripper.gripperBite("cone");
            System.out.println("About to rotate twice...6");
          for (int twice = 0; twice < 2; twice++) {
            turn90Degrees("left"); // turn 180 degrees
          }
            System.out.println("About to drive in power...7");
          driveInPower(0.3, 12.0);
          s_timer.reset();
          s_timer.start();
           System.out.println("About to run arm for 3 seconds...8");
          while (s_timer.get() < 3) { // program runs for 2 sec
            Arm.seatMotors.set(ControlMode.PercentOutput, 0.1); // arm will go down
          }
          Gripper.gripperRetract();
        }

      break;
      case middleauto:
        System.out.println("Attempting middleauto");
        if (autoStatus == 0) {

          autoStatus = 1;
          s_timer.reset();
              System.out.println("driving in powah");
          driveInPower(0.1, 15.0);// drive 10 feet for 5 seconds

        }

        break;
      case shootdock:
        System.out.println("Going to attempt shootdock");
        if (autoStatus == 0) {
             System.out.println("About to extend...1");
          Extend.armExtend(0.5);
          if (autoStatus == 0) {
            autoStatus = 1;
              System.out.println("biting cone..2");
            Gripper.gripperBite("cone");
              System.out.println("eating food!!...3");
            Extend.armRetract(0.4);
               System.out.println("Going to turn left twice...4+5");
            for (int twice = 0; twice < 2; twice++) 
            {
              turn90Degrees("left"); // turn 180 degrees
            }
               System.out.println("Going to drive in power...6");
            driveInPower(0.6, 10.0);
               System.out.println("Going to drive in power again...7");
            driveInPower(0.7, 10.0);
               System.out.println("Going to drive in power again AGAIN...8");
            driveInPower(0.7, 10.0);
               System.out.println("Stopping tank drive!");
            Drive.driveTrain.tankDrive(0.0, 0.0);
               System.out.println("Whomst've'dk'tve'ya'wro'rea'fga?");
          }
        }
      break;

      case kBluDockOnly:
            System.out.println("Attempting to BluDockOnly");
        if (autoStatus == 0) {
          autoStatus = 1;
             System.out.println("Going to let go...1");
          Gripper.gripperRetract();
              System.out.println("Going to drive 2.5 feet backwards...2");
          driveInMultiple(-2.5, 1);
              System.out.println("Going to turn right...3");
          turn90Degrees("right");
              System.out.println("Going to drive 2.5 feet forwards...4");
          driveInMultiple(2.5, 1);
              System.out.println("Going to turn right...5");
          turn90Degrees("right");
              System.out.println("Going to drive 2 feet forwards...6");
          driveInMultiple(2, 1);
              System.out.println("Hooray! I finished!");
        }
      break;

      case kBluSubstation:

        if (autoStatus == 0) {
             System.out.println("Attempting to BlueSubStation");
          autoStatus = 1;
             System.out.println("Going to let go...1");
          Gripper.gripperRetract();
             System.out.println("Going to drive 2.5 feet backwards...2");
          driveInMultiple(-2.5, 1);
             System.out.println("Going to turn right...3");
          turn90Degrees("right");
            System.out.println("Going to turn right again...4");
          turn90Degrees("right");
            System.out.println("Going to drive in muliple...5");
          driveInMultiple(13.66, kDefaultPeriod);
            System.out.println("Peanut Butter Jelly Sandwiches!");
        }
      break;

    }
  }

  @Override
  public void teleopInit() {
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
