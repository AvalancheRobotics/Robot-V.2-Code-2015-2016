package org.usfirst.ftc.exampleteam.yourcodehere.android;

import com.qualcomm.robotcore.hardware.*;
import org.swerverobotics.library.*;
import org.swerverobotics.library.interfaces.*;

/**
 * Autonomous modes:
 *
 *      test()
 *      park(boolean blue)
 *
 * Autonomous support methods:
 *
 *      scoreClimbers(boolean left) < --- NOT TESTED
 *      moveForward(double power, int distance)
 *      pivot(double power, int deg)
 *      turnWithLeftSide(double power, int deg)
 *      turnWithRightSide(double power, int deg)
 *      pivotGyro(int deg) < --- NOT TESTED
 *
 * Low level support methods:
 *
 *      extendSlides()
 *      retractSlides()
 *      releaseClimbers(boolean left)
 *      setAllDrivePower(double power)
 *      pivot(double power)
 *      setLeftDrivePower(double power)
 *      setRightDrivePower(double power)
 */

@Autonomous(name="Auto6253")
public class Auto6253 extends SynchronousOpMode {
    public static final double TICKS_PER_INCH = 133.7;
    public static final double TICKS_PER_DEGREE = (51.84*TICKS_PER_INCH)/360;

    // Declare drive motors
        DcMotor motorLeftFore;
        DcMotor motorLeftAft;
        DcMotor motorRightFore;
        DcMotor motorRightAft;

    // Declare drawer slide motor and servos
    // motor extends/retracts slides
    // servoSlide(continuous) slides the deposit box laterally
    // servoBlockRelease(s) open flaps on the bottom of the bucket, releasing blocks/climbers
        DcMotor motorSlide;
        Servo servoSlide;
        Servo servoBlockReleaseLeft;
        Servo servoBlockReleaseRight;

    // Declare tape measure motor and servo
    // motor extends/retracts tape
    // servo(continuous) angles tape
        DcMotor motorTape;
        Servo servoTape;

    // Declare motor that spins the harvester
        DcMotor motorHarvest;

    // Declare motor that raises and lowers the collection arm
        DcMotor motorArm;

    // Declare zipline flipping servos
        Servo servoLeftZip;
        Servo servoRightZip;

    // Declare sensors
        GyroSensor gyro;

    @Override public void main() throws InterruptedException {
        /* Initialize our hardware variables. Note that the strings used here as parameters
         * to 'get' must correspond to the names you assigned during the robot configuration
         * step you did in the FTC Robot Controller app on the phone.
         */

            // Initialize drive motors
            motorLeftFore = hardwareMap.dcMotor.get("motorLeftFore");
            motorLeftAft = hardwareMap.dcMotor.get("motorLeftAft");
            motorRightFore = hardwareMap.dcMotor.get("motorRightFore");
            motorRightAft= hardwareMap.dcMotor.get("motorRightAft");

            motorRightFore.setDirection(DcMotor.Direction.REVERSE);
            motorRightAft.setDirection(DcMotor.Direction.REVERSE);

            // Initialize drawer slide motor and servos
            motorSlide = hardwareMap.dcMotor.get("motorSlide");
            servoSlide = hardwareMap.servo.get("servoSlide");
            servoBlockReleaseLeft = hardwareMap.servo.get("servoBlockReleaseLeft");
            servoBlockReleaseRight = hardwareMap.servo.get("servoBlockReleaseRight");

            // Initialize tape measure motor and servo
            motorTape = hardwareMap.dcMotor.get("motorTape");
            servoTape = hardwareMap.servo.get("servoTape");

            // Initialize motor that spins the harvester
            motorHarvest = hardwareMap.dcMotor.get("motorHarvest");

            // Initialize motor that raises and lowers the collection arm
            motorArm = hardwareMap.dcMotor.get("motorArm");

            // Initialize zipline flipping servos
            servoLeftZip = hardwareMap.servo.get("servoLeftZip");
            servoRightZip = hardwareMap.servo.get("servoRightZip");

            // Initialize sensors
            gyro = hardwareMap.gyroSensor.get("gyro");

            gyro.calibrate();

        ////////////////////////////Autonomous select via controller////////////////////////////////

        boolean blue = true;
        String autoMode = "test";
        telemetry.addData("alliance", "blue");
        telemetry.addData("auto selected", autoMode);
        telemetry.update();

        while (!isStarted()){
            if(updateGamepads()){
                /////////////////////////// Selects alliance
                if(gamepad1.x)           // x for blue
                    blue = true;         //
                else if(gamepad1.b)      // b for red
                    blue = false;        //
                ///////////////////////////

                ///////////////////////////////////////////////////////// Selects auto mode
                if(gamepad1.dpad_up)                                   // dpad up for test auto
                    autoMode = "test";                                 //
                else if(gamepad1.dpad_down)                            // dpad down for park in the parking zone
                    autoMode = "park";                                 //
                /////////////////////////////////////////////////////////
                if(blue)
                    telemetry.addData("alliance", "blue");
                else
                    telemetry.addData("alliance", "red");
                telemetry.addData("auto selected", autoMode);
                telemetry.update();
            }
        }

        ////////////////////////////Op mode started/////////////////////////////////////////////////

           while (gyro.isCalibrating())
                Thread.sleep(50);

            this.motorLeftAft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
            this.motorRightAft.setMode(DcMotorController.RunMode.RESET_ENCODERS);

        // AT THIS POINT WILL CALL AUTO MODES BASED ON PRELIM BUTTON PRESSES

           if(autoMode.equals("test"))
               test(); // Tests moveForward, pivot, turnWithLeftSide, and turnWithRightSide, pivotGyro, harvest
           else if(autoMode.equals("park"))
               park(blue); // parks in the parking square

        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                              Autonomous modes                                              //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

        public void test() throws InterruptedException{

            telemetry.addData("starting test", "starting moveForward 12");
            telemetry.update();
            moveForward(1, 12);

            telemetry.addData("finished moveForward 12", "starting pivot 90");
            telemetry.update();
            pivot(.5, 90);

            telemetry.addData("finished pivot 90", "starting pivot -90");
            telemetry.update();
            pivot(.5, -90);

            telemetry.addData("finished pivot -90", "starting turnWithLeftSide 90");
            telemetry.update();
            turnWithLeftSide(.5, 90);

            telemetry.addData("finished turnWithLeftSide 90", "starting turnWithLeftSide -90");
            telemetry.update();
            turnWithLeftSide(.5, -90);

            telemetry.addData("finished turnWithLeftSide -90", "starting turnWithRightSide 90");
            telemetry.update();
            turnWithRightSide(.5, 90);

            telemetry.addData("finished turnWithRightSide 90", "starting turnWithRightSide -90");
            telemetry.update();
            turnWithRightSide(.5, -90);

            telemetry.addData("finished turnWithRightSide -90", "starting pivotGyro 90");
            telemetry.update();
            pivotGyro(90);

            telemetry.addData("finished pivotGyro 90", "starting pivotGyro -90");
            telemetry.update();
            pivotGyro(-90);

            telemetry.addData("finished pivotGyro -90", "starting harvest 1");
            telemetry.update();
            motorHarvest.setPower(1);

            telemetry.addData("finished harvest 1", "starting harvest -1");
            telemetry.update();
            motorHarvest.setPower(-1);

            telemetry.addData("finished harvest -1", "starting harvest 0");
            telemetry.update();
            motorHarvest.setPower(0);

            telemetry.addData("finished harvest 0", "finished with test");
            telemetry.update();
        }

        public void park(boolean blue) throws InterruptedException{
            motorHarvest.setPower(-1);
            moveForward(.5, 72);
            if (blue)
                pivot(.5, -90);
            else
                pivot(.5, 90);
            moveForward(.5, 72);
            motorHarvest.setPower(0);
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                              Autonomous support methods                                    //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

        public void scoreClimbers(boolean left) throws InterruptedException{
            extendSlides();
            releaseClimbers(left);
            retractSlides();
        }

        public void moveForward(double power, int distance) throws InterruptedException{
            int ticks = (int)(TICKS_PER_INCH * distance);

            motorLeftAft.setTargetPosition(motorLeftAft.getCurrentPosition() + ticks);
            motorRightAft.setTargetPosition(motorRightAft.getCurrentPosition() + ticks);

            setAllDrivePower(power);

            motorLeftAft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motorRightAft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

            while (motorLeftAft.isBusy() || motorRightAft.isBusy())
                this.idle();

            setAllDrivePower(0);

        }

        public void pivot(double power, int deg) throws InterruptedException{
            int ticks = (int)(TICKS_PER_DEGREE * deg);

            motorLeftFore.setTargetPosition(motorLeftFore.getCurrentPosition() + ticks);
            motorLeftAft.setTargetPosition(motorLeftAft.getCurrentPosition() + ticks);
            motorRightFore.setTargetPosition(motorRightFore.getCurrentPosition() - ticks);
            motorRightAft.setTargetPosition(motorRightAft.getCurrentPosition() - ticks);

            setLeftDrivePower(power);
            setRightDrivePower(power);

            motorLeftFore.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motorLeftAft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motorRightFore.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motorRightAft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

            while (motorLeftAft.isBusy() || motorRightAft.isBusy())
                this.idle();

            setAllDrivePower(0);

        }

        public void turnWithLeftSide(double power, int deg) throws InterruptedException{
            int ticks = (int)(TICKS_PER_DEGREE * deg * 2);

            motorLeftFore.setTargetPosition(motorLeftFore.getCurrentPosition()+ ticks);
            motorLeftAft.setTargetPosition(motorLeftAft.getCurrentPosition() + ticks);

            setLeftDrivePower(power);

            motorLeftFore.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motorLeftAft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

            while (motorLeftAft.isBusy())
                this.idle();

            setAllDrivePower(0);

        }

        public void turnWithRightSide(double power, int deg) throws InterruptedException{
            int ticks = (int)(TICKS_PER_DEGREE * deg * 2);

            motorRightFore.setTargetPosition(motorRightFore.getCurrentPosition() + ticks);
            motorRightAft.setTargetPosition(motorRightAft.getCurrentPosition() + ticks);

            setRightDrivePower(power);

            motorRightFore.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
            motorRightAft.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

            while (motorRightAft.isBusy())
                this.idle();

            setAllDrivePower(0);

        }

        public void pivotGyro(int deg) throws InterruptedException{
            double power;
            double proportionalConst = 0.005;
            double topCeiling = 1;
            double bottomCeiling = -1;
            double topFloor = .12;
            double bottomFloor = -.12;
            int target = gyro.getHeading() + deg;

            while(target != gyro.getHeading()){
                power = (target - gyro.getHeading()) * proportionalConst;
                if(power > topCeiling)
                    power = topCeiling;
                else if(power < bottomCeiling )
                    power = bottomCeiling;
                else if(power < topFloor && power > 0)
                    power = topFloor;
                else if(power > bottomFloor && power < 0)
                    power = bottomFloor;
                pivot(power);
            }

            setAllDrivePower(0);
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                              Low level support methods                                     //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

        public void extendSlides() throws InterruptedException{
            int distance = 12;
            double power = .5;

            motorSlide.setTargetPosition(motorSlide.getCurrentPosition() + distance);

            motorSlide.setPower(power);
            motorSlide.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

            while (motorSlide.isBusy())
                this.idle();

            motorSlide.setPower(0);
        }
        public void retractSlides() throws InterruptedException{
            int distance = -12;
            double power = .5;

            motorSlide.setTargetPosition(motorSlide.getCurrentPosition() + distance);

            motorSlide.setPower(power);
            motorSlide.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

            while (motorSlide.isBusy())
                this.idle();

            motorSlide.setPower(0);
        }
        public void releaseClimbers(boolean left) throws InterruptedException{
            if(left){
                servoBlockReleaseLeft.setPosition(.25);
                wait(5000);
                servoBlockReleaseLeft.setPosition(1);
            } else{
                servoBlockReleaseRight.setPosition(.25);
                wait(5000);
                servoBlockReleaseRight.setPosition(1);
            }
        }
        public void setAllDrivePower(double power){
            motorLeftFore.setPower(power);
            motorLeftAft.setPower(power);
            motorRightFore.setPower(power);
            motorRightAft.setPower(power);
        }
        public void pivot(double power){
            motorLeftFore.setPower(power);
            motorLeftAft.setPower(power);
            motorRightFore.setPower(-power);
            motorRightAft.setPower(-power);
        }
        public void setLeftDrivePower(double power){
            motorLeftFore.setPower(power);
            motorLeftAft.setPower(power);
        }
        public void setRightDrivePower(double power){
            motorRightFore.setPower(power);
            motorRightAft.setPower(power);
        }
}
