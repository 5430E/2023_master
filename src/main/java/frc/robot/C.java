package frc.robot;

public class C {
    public static final class Drivestyle{
                //Drive Style definition, loop up. Don't change this! This is needed for the subsystem.
                public static final int tankdrive = 1;
                public static final int chezydrive = 2;
        
                //Drive. We can change this.
                public static int drivestyle = chezydrive;
                public static int invert = -1;
        
                public static double slowModeScaleFactor = 0.25;
        
                //Tuning the Chezy Drive - deadband, sensitivity & tolerancing values on raw joystick inputs
                public static final double throttleDeadband = 0.02; 
                public static final double wheelDeadband = 0.02;	
                public static final double sensitivityHigh = 0.5;	
                public static final double sensitivityLow = 0.5; // (**deafult uses low gear**)
        
        
                public static final double wheelNonLinearityHighGear = 0.5; //Chezy Drive non-linearity
                public static final double wheelNonLinearityLowGear = 0.6; //Chezy Drive non-linearity  (**deafult uses low gear**)
        
                public static final double QuickTurnSensitivityHigh = 0.005; //Chezy Drive quick turn sensitivity
                public static final double QuickTurnSensitivityLow = 0.005; //Chezy Drive quick turn sensitivity  (**deafult uses low gear**)
    }
}
