package be.unamur.binny.animation

import scalafx.animation.{KeyFrame, Timeline, TranslateTransition}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.util.Duration

import be.unamur.binny.SharedState

import scala.util.Random

class VirtualAssistantEyes(sharedState: SharedState) extends JFXApp3 {

	private val eyeRadius: Float = 70.0   // Taille des yeux
	private val pupilRadius: Float = 35.0 // Taille des pupilles
	private val movementRange: Float = eyeRadius - pupilRadius - 5 // Limite de déplacement des pupilles (reste dans l'œil)

	// Création des yeux (grands cercles blancs)
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

	// Création des pupilles (petits cercles noirs)
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

	// Définir les mouvements des pupilles
	private def movePupils(dx: Double, dy: Double): Unit = {
		def clamp(value: Double, min: Double, max: Double): Double = {
			Math.max(min, Math.min(max, value))
		}

		val newLeftX = clamp(leftPupil.centerX() + dx, leftEye.centerX() - movementRange, leftEye.centerX() + movementRange)
		val newLeftY = clamp(leftPupil.centerY() + dy, leftEye.centerY() - movementRange, leftEye.centerY() + movementRange)

		val newRightX = clamp(rightPupil.centerX() + dx, rightEye.centerX() - movementRange, rightEye.centerX() + movementRange)
		val newRightY = clamp(rightPupil.centerY() + dy, rightEye.centerY() - movementRange, rightEye.centerY() + movementRange)

		new TranslateTransition {
			duration = Duration(500)
			node = leftPupil
			toX = newLeftX - leftEye.centerX()
			toY = newLeftY - leftEye.centerY()
		}.play()

		new TranslateTransition {
			duration = Duration(500)
			node = rightPupil
			toX = newRightX - rightEye.centerX()
			toY = newRightY - rightEye.centerY()
		}.play()
	}

	// Animation de clignement des yeux (les pupilles disparaissent)
	private def performBlink(): Unit = {
		leftPupil.visible = false
		rightPupil.visible = false

		leftEye.scaleY = 0.1
		rightEye.scaleY = 0.1
		rightEye.fill= Color.Black
		leftEye.fill = Color.Black
		new Timeline {
			keyFrames = Seq(
				KeyFrame(Duration(100), onFinished = _ => {
					leftEye.scaleY = 1.0
					rightEye.scaleY = 1.0
					leftPupil.visible = true
					rightPupil.visible = true
					rightEye.fill= Color.White
					leftEye.fill = Color.White
				})
			)
		}.play()
	}

	// Sélection aléatoire d'une animation
	private def playRandomAnimation(): Unit = {
		Random.nextInt(5) match {
			case 0 => movePupils(0, -movementRange) // Regard en haut
			case 1 => movePupils(0, movementRange)  // Regard en bas
			case 2 => movePupils(-movementRange, 0) // Regard à gauche
			case 3 => movePupils(movementRange, 0)  // Regard à droite
			case 4 => performBlink()               // Clignement
		}
	}

	// Boucle des animations
	private val animationLoop: Timeline = new Timeline {
		cycleCount = Timeline.Indefinite
		keyFrames = Seq(
			KeyFrame(Duration(1000), onFinished = _ => playRandomAnimation())
		)
	}

	override def start(): Unit = {
		animationLoop.play()

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
