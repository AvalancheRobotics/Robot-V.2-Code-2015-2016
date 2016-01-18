package org.usfirst.ftc.exampleteam.yourcodehere;

import com.qualcomm.robotcore.hardware.*;

import org.swerverobotics.library.*;
import org.swerverobotics.library.interfaces.*;

/**
 * Version 1.0 of Team Avalanche 6253's TeleOp program for Robot version 2.0.
 * Currently most distance and position values are arbitrary due to not having a complete robot we can test values on.
 */
@TeleOp(name = "TeleOpV1")
public class TeleOpV1 extends SynchronousOpMode {
    // Variables

    //Declare ModifiedBoolean methods. These methods decide whether an action is running or not.
    private ModifiedBoolean testRunning = new ModifiedBoolean(false);
    private ModifiedBoolean runningExtendTapeAuto = new ModifiedBoolean(false);
    private ModifiedBoolean runningCancelAllArm = new ModifiedBoolean(false);
    private ModifiedBoolean runningCancelAllTape = new ModifiedBoolean(false);
    private ModifiedBoolean runningCancelAllSlide = new ModifiedBoolean(false);
    private ModifiedBoolean runningExtendSlide = new ModifiedBoolean(false);
    private ModifiedBoolean runningRetractSlide = new ModifiedBoolean(false);

    //shows what step score is on
    private int scoreToggle = 0;

    // defaults to blue alliance
    private boolean isBlue = true;

    //tells whether triggers are in resting position or are down/active, starts at rest
    private boolean atRestTriggers = true;

    // methods with these variables need values
    private final double ARBITRARYDOUBLE = 0;
    private final int ARBITRARYINT = 0;
    private final boolean NEEDS_BUTTON_MAPPED = false;

    //Declare starting positions for tape, arm, and slide motors
    private int startPosTape;
    private int startPosArm;
    private int startPosSlide;

    private final int DISTANCE_TO_HANG = ARBITRARYINT;
    private final double NEEDS_THRESH = ARBITRARYDOUBLE;
    private final double RIGHT_ZIP_UP = ARBITRARYDOUBLE;
    private final double RIGHT_ZIP_DOWN = ARBITRARYDOUBLE;
    private final double LEFT_ZIP_UP = ARBITRARYDOUBLE;
    private final double LEFT_ZIP_DOWN = ARBITRARYDOUBLE;
    private final int TICKS_IN_INCH_AFT = ARBITRARYINT;
    private final int TICKS_IN_INCH_FORE = ARBITRARYINT;
    private final int TICKS_IN_INCH_TAPE = ARBITRARYINT;
    private final int TICKS_IN_DEGREE_ARM = ARBITRARYINT;
    private final int TICKS_IN_INCH_SLIDE = ARBITRARYINT;
    private final double DISTANCE_TO_TOP = ARBITRARYDOUBLE;
    private final double DISTANCE_TO_MID = ARBITRARYDOUBLE;
    private final double DISTANCE_TO_BOT = ARBITRARYDOUBLE;
    private final double LOCK_ENGAGED = ARBITRARYDOUBLE;
    private final double LOCK_DISENGAGED = ARBITRARYDOUBLE;


    // Declare drive motors
    DcMotor motorLeftFore;
    DcMotor motorLeftAft;
    DcMotor motorRightFore;
    DcMotor motorRightAft;

    // Declare drawer slide motor and servos
    // motor extends/retracts slides
    // servoSlide(continuous) slides the deposit box laterally
    // servoConveyor runs the conveyor belt, dispensing blocks over the side
    // servoLock is the servo for locking the tape measure in place once hanging.
    DcMotor motorSlide;
    Servo servoSlide;
    Servo servoConveyor;
    Servo servoLock;

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

    @Override
    public void main() throws InterruptedException {
        hardwareMapping();

        waitForStart();
        // Go go gadget robot!
        while (opModeIsActive()) {
            if (updateGamepads()) {

                //AUTOMATIC CONTROLS//


                //TESTING PURPOSES
                if (gamepad1.b) {
                    testRunning.toggle();
                    if (testRunning.getValue()) {
                        motorSlide.setPower(0);
                    } else {
                        motorSlide.setTargetPosition(motorSlide.getCurrentPosition() + 1680 * 5);
                    }
                }

                //MANUAL CONTROLS
                manualMethods();

            }

            runAllAutoMethods();

            if (testRunning.getValue()) {
                testContMotor();
            }
            idle();
        }
    }


    private void manualMethods() {
        //Toggle Team (if we need to score on an opponent's ramp)
        if ((gamepad1.back && gamepad1.b) || (gamepad2.back && gamepad2.b))
            isBlue = false;
        if ((gamepad1.back && gamepad1.x) || (gamepad2.back && gamepad2.x))
            isBlue = true;

            extendTapeManual(gamepad2.dpad_up);
        retractTapeManual(gamepad2.dpad_down);

        //starts and stops harvester
        if (gamepad1.left_trigger > .8)
            toggleHarvester(1);

        //toggles harvester spin direction
        if (gamepad1.left_bumper)
            reverseHarvester();

        //Read Joystick Data and Update Speed of Left and Right Motors
        //If joystick buttons are pressed, sets drive power to preset value
        manualDriveControls();

        if (gamepad2.x)
            triggerZipline();

        if ((gamepad1.back && gamepad1.start) || (gamepad2.back && gamepad2.start))
            cancelAll();

        //Stops any auto methods using slides and manually controls power with joysticks
       if (gamepad2.left_stick_y < -.2 || gamepad2.left_stick_y > .2) {
           motorSlide.setPower(scaleInput(gamepad2.left_stick_y));
           runningExtendSlide.setFalse();
           runningRetractSlide.setFalse();
       }

        if (gamepad2.left_bumper) {
            servoSlide.setPosition(ARBITRARYDOUBLE);
        }
        if (gamepad2.right_bumper) {
            servoSlide.setPosition(-ARBITRARYDOUBLE);
        }

        //manually adjust the conveyors
        if (gamepad2.right_trigger > .2)
            servoConveyor.setPosition(gamepad2.right_trigger);
        if (gamepad2.left_trigger > .2)
            servoConveyor.setPosition(gamepad2.left_trigger);

    }

    /**
     * put all autonomous setters in here, when the code updates the gamepad
     * it should look at this section to see if we're starting any auto methods
     * DO NOT PUT MANUAL METHODS- Separate Wrapper Method
     */
    private void setAllAutoMethods() {
        if (gamepad1.x) {
            telemetry.addData("Button Works!", "Test");
            telemetry.update();
        }

        /**
         * Scoring methods
         * First button press extends slides and shuttle
         * Second button press starts conveyor
         * Third button press stops conveyor, and restores slides and shuttle to default positions
         */
        scoreSet("bot", gamepad2.a);
        scoreSet("mid", gamepad2.b);
        scoreSet("top", gamepad2.y);

    }

    /**
     * put all autonomous functions in here, to be run after code finishes reading gamepad
     * this method runs approx every 35 ms
     * this section is for setting the motor power for all of the automatic
     * methods that we call in the gamepad section
     */
    private void runAllAutoMethods() {
        //Run tape motor till it reaches hang distance
        runToPos(motorTape, ARBITRARYDOUBLE, runningExtendTapeAuto, null);
    }

    //Initialize and Map All Hardware
    private void hardwareMapping() throws InterruptedException {
        // Initialize drive motors
        motorLeftFore = hardwareMap.dcMotor.get("motorLeftFore");
        motorLeftAft = hardwareMap.dcMotor.get("motorLeftAft");
        motorRightFore = hardwareMap.dcMotor.get("motorRightFore");
        motorRightAft = hardwareMap.dcMotor.get("motorRightAft");

        //Left and right motors are on opposite sides and must spin opposite directions to go forward
        motorLeftAft.setDirection(DcMotor.Direction.REVERSE);
        motorLeftFore.setDirection(DcMotor.Direction.REVERSE);

        // Initialize drawer slide motor and servos
        motorSlide = hardwareMap.dcMotor.get("motorSlide");
        servoSlide = hardwareMap.servo.get("servoSlide");
        servoConveyor = hardwareMap.servo.get("servoConveyor");

        // Initialize tape measure motor, servo tape, and servo lock.
        motorTape = hardwareMap.dcMotor.get("motorTape");
        servoTape = hardwareMap.servo.get("servoTape");
        servoLock = hardwareMap.servo.get("servoLock");

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

        // Reset encoders
        this.motorLeftAft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        this.motorLeftFore.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        this.motorTape.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        this.motorHarvest.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        this.motorArm.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        this.motorRightAft.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        this.motorRightFore.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        this.motorSlide.setMode(DcMotorController.RunMode.RESET_ENCODERS);

        //Set Runmode for all motors to run using encoders
        motorLeftFore.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motorRightFore.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motorLeftAft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motorRightAft.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motorSlide.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motorTape.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motorArm.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        motorHarvest.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        //keep track of the starting positions of arm, slide, and tape motors
        startPosArm = motorArm.getCurrentPosition();
        startPosSlide = motorSlide.getCurrentPosition();
        startPosTape = motorSlide.getCurrentPosition();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                     main methods                                           //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void triggerZipline() {
        if (isBlue) {
            if (atRestTriggers)
                servoRightZip.setPosition(RIGHT_ZIP_UP); //Needs resting and active servo positions
            else
                servoRightZip.setPosition(RIGHT_ZIP_DOWN);
        } else {
            if (atRestTriggers)
                servoLeftZip.setPosition(RIGHT_ZIP_UP);
            else
                servoLeftZip.setPosition(RIGHT_ZIP_DOWN);
        }
        atRestTriggers = !atRestTriggers;
    }

    //retracts the tape until it reaches just a little above the starting position at the time of initialization
    private void retractTapeAuto() throws InterruptedException {
        if (motorTape.isBusy())
            motorTape.setPower(0);
        else {
            // moveToPosTicks(motorTapeStartPos, motorTape, NEEDS_THRESH);
            while (motorTape.isBusy())
                this.idle();
        }
    }

    private void extendTapeManual(boolean b) {
        if (b) {
            if (servoLock.getPosition() != LOCK_DISENGAGED) {
                servoLock.setPosition(LOCK_DISENGAGED);
            } else
                motorTape.setPower(1);
        } else {
            if (servoTape.getPosition() != LOCK_ENGAGED)
                servoLock.setPosition(LOCK_ENGAGED);
            motorTape.setPower(0.0);
        }
    }

    private void retractTapeManual(boolean b) {
        if (b) {
            if (servoLock.getPosition() != LOCK_DISENGAGED) {
                servoLock.setPosition(LOCK_DISENGAGED);
            } else {
                motorTape.setPower(-1);
            }
        } else {
            if (servoTape.getPosition() != LOCK_ENGAGED)
                servoLock.setPosition(LOCK_ENGAGED);
            motorTape.setPower(0.0);
        }
    }

    private void toggleHarvester(double power) {
        if (motorHarvest.getPower() == 0.0)
            motorHarvest.setPower(power);
        else
            motorHarvest.setPower(0.0);
    }

    private void reverseHarvester() {
        motorHarvest.setDirection(DcMotor.Direction.REVERSE);
    }

    //return robot to initialization position
    private void initPositionSet() {
        //sets harvester and drive power to 0
        motorHarvest.setPower(0);
        setLeftDrivePower(0);
        setRightDrivePower(0);

        //sets the target position for arm, slide, and tape to the init positions
        setPosMotor(motorArm, runningCancelAllArm, 1, startPosArm);
        setPosMotor(motorTape, runningCancelAllTape, 1, startPosTape);
        setPosMotor(motorSlide, runningCancelAllSlide, 1, startPosSlide);
    }

    //runs until the motors hit the initialization position
    private void initPositionRun() {
        if (runningCancelAllArm.getValue() || runningCancelAllSlide.getValue() || runningCancelAllTape.getValue()) {
            runToPos(motorArm, 50, runningCancelAllArm, null);
            runToPos(motorArm, 50, runningCancelAllTape, null);
            runToPos(motorSlide, 50, runningCancelAllTape, null);
        }
    }

    //stop all motors
    private void cancelAll() {
        motorArm.setPower(0);
        motorHarvest.setPower(0);
        motorTape.setPower(0);
        motorSlide.setPower(0);
        setLeftDrivePower(0);
        setRightDrivePower(0);
        /**
         * ADD CODE HERE THAT SETS ALL MODIFIEDBOOLEANS EQUAL TO FALSE WHEN EVERYTHING ELSE IS DONE
         */
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                                                                            //
    //                                     support methods                                        //
    //                                                                                            //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setPosMotor(DcMotor motor, ModifiedBoolean isRunning, int ticksInInch, double distance) {
        isRunning.toggle();
        if (!isRunning.getValue()) {
            motor.setPower(0);
        } else
            motor.setTargetPosition((int) (motor.getCurrentPosition() + (ticksInInch * distance)));
    }


    private void runToPos(DcMotor motor, double minPower, ModifiedBoolean isRunning, ModifiedBoolean runNext) {
        if (isRunning.getValue()) {
            if (motor.getCurrentPosition() - 10 < motor.getTargetPosition() && motor.getCurrentPosition() < motor.getTargetPosition() + 10) {
                motor.setPower(0);
                isRunning.setFalse();
                if (runNext != null)
                    runNext.setTrue();
            } else
                setCurvedPower(motor, ARBITRARYDOUBLE, 1, minPower);
        }
    }

    private void setLeftDrivePower(double power) {
        motorLeftFore.setPower(power);
        motorLeftAft.setPower(power);
    }

    private void setRightDrivePower(double power) {
        motorRightFore.setPower(power);
        motorRightAft.setPower(power);
    }

    private void setCurvedPower(DcMotor motor, double thresh, double inputPower, double minPower) {
        int target = motor.getTargetPosition();
        if (target - motor.getCurrentPosition() > thresh)
            motor.setPower(inputPower);
        else {
            double overThr = motor.getCurrentPosition() - target + thresh;
            double power = (inputPower - minPower) / (1 + Math.pow(Math.E, Math.pow(overThr / thresh - .5, -4) * 12)) + minPower;
            motor.setPower(power);
        }
    }

    //Default Scale Input Method created by FTC- We will use this one until someone creates a better one.
    //Used for scaling joysticks, basically is a floor function that is squared
    double scaleInput(double dVal) {
        double[] scaleArray = {0.0, 0.05, 0.09, 0.10, 0.12, 0.15, 0.18, 0.24,
                0.30, 0.36, 0.43, 0.50, 0.60, 0.72, 0.85, 1.00, 1.00};

        // get the corresponding index for the scaleInput array.
        int index = (int) (dVal * 16.0);
        if (index < 0)
            index = -index;
        if (index > 16)
            index = 16;


        double dScale;
        if (dVal < 0)
            dScale = -scaleArray[index];
        else
            dScale = scaleArray[index];

        return dScale;
    }

    private void testContMotor() {
        if (motorSlide.getCurrentPosition() < motorSlide.getTargetPosition()) {
            setCurvedPower(motorSlide, 1000, 1, 0.3);
        } else {
            motorSlide.setPower(0);
            testRunning.setFalse();
        }
    }

    //This method is a combination of autonomous and manual controls
    //Certain actions such as the conveyor run on button prompts while
    //Other automated actions such as shuttling run on their own
    private void scoreSet(String height, boolean button) {  // can be either top mid or bot
        if (scoreToggle == 0) {
            //extend slides to specified height
            extendSlideSet(height);
            shuttleDispenserSet(true); //NEED TO WRITE METHOD AFTER FIGURING OUT WHAT WE'RE DOING TOUCH SENSOR? TIME?
            scoreToggle++;
            return;
        }

        if (scoreToggle == 1) {
            //start the conveyor
            servoConveyor.setPosition(ARBITRARYDOUBLE);
            scoreToggle++;
            return;
        }
        if (scoreToggle == 2) {
            //stop conveyor and return to retracted position
            servoConveyor.setPosition(ARBITRARYDOUBLE);
            shuttleDispenserSet(false); //NEED TO WRITE METHOD AFTER FIGURING OUT WHAT WE'RE DOING TOUCH SENSOR? TIME?
            setPosMotor(motorSlide, runningRetractSlide, 1, startPosSlide);
            scoreToggle = 0;
        }
        /**
         * NEED TO WRITE METHOD THAT CAN SHUTTLE THE DISPENSER TO A SPECIFIC DISTANCE - most likely
         * will be time based and refined through testing since continuous servos don't have
         * encoders or positions
         */
    }

    private void scoreRun() {
        //Extends Tape Measure
        runToPos(motorSlide, ARBITRARYDOUBLE, runningExtendSlide, null);
        shuttleDispenserRun();
        //NEED TO WRITE METHOD THAT CAN SHUTTLE THE DISPENSER TO SPECIFIC DISTANCE
        runToPos(motorSlide, ARBITRARYDOUBLE, runningRetractSlide, null);
    }

    /**
     * WRITE METHOD
     */
    private void shuttleDispenserRun() {

    }

    /**
     * WRITE METHOD
     */
    private void shuttleDispenserSet(boolean toGoal) {

    }

    private void loadDispenserSet() {

    }


    private void dumpDispenser() {

    }

    //returns and telemetry updates are for testing purposes
    private void extendSlideSet(String height) { //Can be either top mid or bot
        if (height.equals("top")) {
            setPosMotor(motorSlide, runningExtendSlide, TICKS_IN_INCH_SLIDE, DISTANCE_TO_TOP);
            return;
        }

        if (height.equals("mid")) {
            setPosMotor(motorSlide, runningExtendSlide, TICKS_IN_INCH_SLIDE, DISTANCE_TO_MID);
            return;
        }

        if (height.equals("bot")) {
            setPosMotor(motorSlide, runningExtendSlide, TICKS_IN_INCH_SLIDE, DISTANCE_TO_BOT);
            return;
        }

        telemetry.addData("NOT A VALID HEIGHT FOR SLIDES", "FIX CODE");
        telemetry.update();
    }

    private void manualDriveControls() {

        if (gamepad1.left_stick_button) {
            if (gamepad1.left_stick_y > 0)
                setLeftDrivePower(ARBITRARYDOUBLE);
            else
                setLeftDrivePower(-ARBITRARYDOUBLE);
        } else
            setLeftDrivePower(scaleInput(gamepad1.left_stick_y));

        if (gamepad1.right_stick_button) {
            if (gamepad1.right_stick_y > 0)
                setRightDrivePower(ARBITRARYDOUBLE);
            else
                setRightDrivePower(-ARBITRARYDOUBLE);
        } else
            setRightDrivePower(scaleInput(gamepad1.right_stick_y));

    }

}
