/*
 * Copyright (c) 2025 Newport Robotics Group. All Rights Reserved.
 *
 * Open Source Software; you can modify and/or share it under the terms of
 * the license file in the root directory of this project.
 */
 
package frc.robot;

import com.nrg948.preferences.RobotPreferences;
import com.nrg948.preferences.RobotPreferencesLayout;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.AlgaeCommands;
import frc.robot.commands.ClimberCommands;
import frc.robot.commands.CoralCommands;
import frc.robot.commands.DriveCommands;
import frc.robot.commands.DriveUsingController;
import frc.robot.commands.ElevatorCommands;
import frc.robot.commands.FlameCycle;
import frc.robot.commands.LEDCommands;
import frc.robot.commands.ManipulatorCommands;
import frc.robot.parameters.ElevatorLevel;
import frc.robot.subsystems.Subsystems;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
@RobotPreferencesLayout(groupName = "Preferences", column = 0, row = 0, width = 1, height = 1)
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final Subsystems subsystems = new Subsystems();
  private final RobotAutonomous autonomous =
      new RobotAutonomous(subsystems, null); // TODO: figure out what rotaion feedback override.

  // Replace with CommandPS4Controller or CommandJoystick if needed
  private final CommandXboxController m_driverController =
      new CommandXboxController(OperatorConstants.DRIVER_CONTROLLER_PORT);

  private final CommandXboxController m_manipulatorController =
      new CommandXboxController(OperatorConstants.MANIPULATOR_CONTROLLER_PORT);

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    initShuffleboard();

    subsystems.drivetrain.setDefaultCommand(
        new DriveUsingController(subsystems, m_driverController));

    subsystems.statusLEDs.setDefaultCommand(new FlameCycle(subsystems.statusLEDs));

    // Configure the trigger bindings
    configureBindings();
  }

  public void disabledInit() {
    subsystems.disable();
  }

  private void initShuffleboard() {
    RobotPreferences.addShuffleBoardTab();

    subsystems.initShuffleboard();

    ShuffleboardTab operatorTab = Shuffleboard.getTab("Operator");
    autonomous.addShuffleboardLayout(operatorTab);
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    m_driverController.start().onTrue(DriveCommands.resetOrientation(subsystems));
    m_driverController.x().whileTrue(DriveCommands.alignToLeftBranch(subsystems));
    m_driverController.b().whileTrue(DriveCommands.alignToRightBranch(subsystems));
    m_driverController.rightBumper().whileTrue(ClimberCommands.climb(subsystems));

    m_manipulatorController
        .a()
        .onTrue(ElevatorCommands.goToElevatorLevel(subsystems, ElevatorLevel.L1));
    m_manipulatorController
        .x()
        .onTrue(ElevatorCommands.goToElevatorLevel(subsystems, ElevatorLevel.L2));
    m_manipulatorController
        .b()
        .onTrue(ElevatorCommands.goToElevatorLevel(subsystems, ElevatorLevel.L3));
    m_manipulatorController
        .y()
        .onTrue(ElevatorCommands.goToElevatorLevel(subsystems, ElevatorLevel.L4));
    m_manipulatorController.rightBumper().whileTrue(AlgaeCommands.intakeAlgae(subsystems));
    m_manipulatorController.rightBumper().onFalse(AlgaeCommands.stopAndStowIntake(subsystems));
    m_manipulatorController.leftBumper().whileTrue(AlgaeCommands.outtakeAlgae(subsystems));
    m_manipulatorController.leftBumper().onFalse(AlgaeCommands.stopAndStowIntake(subsystems));
    m_manipulatorController.povLeft().whileTrue(CoralCommands.intakeUntilCoralDetected(subsystems));
    m_manipulatorController
        .povRight()
        .whileTrue(CoralCommands.outtakeUntilCoralNotDetected(subsystems));
    m_manipulatorController.povRight().onFalse(ElevatorCommands.stowElevatorAndArm(subsystems));
    m_manipulatorController.start().onTrue(ElevatorCommands.stowElevatorAndArm(subsystems));
    m_manipulatorController.back().onTrue(ManipulatorCommands.interruptAll(subsystems));
    m_manipulatorController
        .povDown()
        .whileTrue(AlgaeCommands.removeAlgaeAtLevel(subsystems, ElevatorLevel.L2));
    m_manipulatorController.povDown().onFalse(ElevatorCommands.stowElevatorAndArm(subsystems));
    m_manipulatorController
        .povUp()
        .whileTrue(AlgaeCommands.removeAlgaeAtLevel(subsystems, ElevatorLevel.L3));
    m_manipulatorController.povUp().onFalse(ElevatorCommands.stowElevatorAndArm(subsystems));

    new Trigger(subsystems.coralRoller::hasCoral)
        .onTrue(LEDCommands.indicateCoralAcquired(subsystems));
    new Trigger(subsystems.algaeGrabber::hasAlgae)
        .onTrue(LEDCommands.indicateAlgaeAcquired(subsystems));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autonomous.getAutonomousCommand(subsystems);
  }

  public void periodic() {
    subsystems.periodic();
  }
}
