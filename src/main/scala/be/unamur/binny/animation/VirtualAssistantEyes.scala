package be.unamur.binny.animation

import scalafx.animation.{KeyFrame, ScaleTransition, Timeline, TranslateTransition}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.util.Duration

import scala.util.Random

class VirtualAssistantEyes extends JFXApp3 {

	private val eyeRadius: Float = 70.0   // Taille des yeux
	private val pupilRadius: Float = 35.0 // Taille des pupilles
	private val movementRange: Float = 15  // Limite de déplacement des pupilles

	// Création des yeux (les grands cercles blancs)
	private val leftEye: Circle = new Circle {
		centerX = 150
		centerY = 160
		radius = eyeRadius
		fill = Color.White
		stroke = Color.Black
		strokeWidth = 2
	}

	private val rightEye: Circle = new Circle {
		centerX = 330
		centerY = 160
		radius = eyeRadius
		fill = Color.White
		stroke = Color.Black
		strokeWidth = 2
	}

	// Création des pupilles (les petits cercles noirs)
	private val leftPupil: Circle = new Circle {
		centerX = leftEye.centerX()
		centerY = leftEye.centerY()
		radius = pupilRadius
		fill = Color.Black
	}

	private val rightPupil: Circle = new Circle {
		centerX = rightEye.centerX()
		centerY = rightEye.centerY()
		radius = pupilRadius
		fill = Color.Black
	}

	// Animation qui déplace les pupilles de gauche à droite
	private val eyeMovementTimeline: Timeline = new Timeline {
		cycleCount = Timeline.Indefinite
		keyFrames = Seq(
			KeyFrame(Duration(0), onFinished = _ => {
				leftPupil.centerX = leftEye.centerX() - movementRange
				rightPupil.centerX = rightEye.centerX() - movementRange
			}),
			KeyFrame(Duration(500), onFinished = _ => {
				leftPupil.centerX = leftEye.centerX() + movementRange
				rightPupil.centerX = rightEye.centerX() + movementRange
			}),
			KeyFrame(Duration(1000), onFinished = _ => {
				leftPupil.centerX = leftEye.centerX() - movementRange
				rightPupil.centerX = rightEye.centerX() - movementRange
			})
		)
	}

	// Animation qui déplace les pupilles de gauche à droite
	private val leftPupilTransition: TranslateTransition = new TranslateTransition {
		duration = Duration(1000)
		node = leftPupil
		fromX = -movementRange
		toX = movementRange
		autoReverse = true
		cycleCount = TranslateTransition.Indefinite
	}

	private val rightPupilTransition: TranslateTransition = new TranslateTransition {
		duration = Duration(1000)
		node = rightPupil
		fromX = -movementRange
		toX = movementRange
		autoReverse = true
		cycleCount = TranslateTransition.Indefinite
	}

	override def start(): Unit = {
		// Démarrage de l'animation au lancement
//		eyeMovementTimeline.play() // En mode teleportation
		leftPupilTransition.play()
		rightPupilTransition.play()

		stage = new PrimaryStage {
			title = "Virtual Assistant Eyes Animation"
			scene = new Scene(480, 320) {
				content = List(leftEye, rightEye, leftPupil, rightPupil)
				fill = Color.LightBlue
				cursor = scalafx.scene.Cursor.None
			}
			fullScreen = true
		}
	}
}