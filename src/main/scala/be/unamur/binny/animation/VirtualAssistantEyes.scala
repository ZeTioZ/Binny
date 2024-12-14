package be.unamur.binny.animation

import scalafx.animation.{KeyFrame, Timeline, TranslateTransition}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.SceneIncludes.jfxScene2sfx
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Rectangle}
import scalafx.util.Duration

import be.unamur.binny.SharedState

import scala.util.Random

class VirtualAssistantEyes(sharedState: SharedState) extends JFXApp3
{
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

	//Background pour la progressbar
	private val progressBarBackground: Rectangle = new Rectangle {
		x = 100
		y = 260
		width = 280
		height = 20
		fill = Color.Gray
		arcWidth = 10
		arcHeight = 10
	}

	//ProgressBar Dynamique
	private val progressBar: Rectangle = new Rectangle {
		x = 100
		y = 260
		width = 0
		height = 20
		fill = Color.Green
		arcWidth = 10
		arcHeight = 10
	}

	//Animation pour la progressbar
	private val progressBarUpdater: Timeline = new Timeline {
		cycleCount = Timeline.Indefinite
		var open = false
		keyFrames = Seq(
			KeyFrame(Duration(100), onFinished = _ =>
			{
				if (sharedState.servoAngle == 90)
				{
					if (open)
					{
						open = false
						Thread.sleep(1000)
					}
					val progress = (564.0 - sharedState.lidDistance) / 564.0
					progressBar.width = progress * progressBarBackground.width()
				}
				else
				{
					open = true
				}
			})
		)
	}

	// Définir les mouvements des pupilles
	private def movePupils(dx: Double, dy: Double): Unit =
	{
		def clamp(value: Double, min: Double, max: Double): Double =
		{
			Math.max(min, Math.min(max, value))
		}

		val newLeftX = clamp(leftPupil.centerX() + dx, leftEye.centerX() - movementRange, leftEye.centerX() + movementRange)
		val newLeftY = clamp(leftPupil.centerY() + dy, leftEye.centerY() - movementRange, leftEye.centerY() + movementRange)

		val newRightX = clamp(rightPupil.centerX() + dx, rightEye.centerX() - movementRange, rightEye.centerX() + movementRange)
		val newRightY = clamp(rightPupil.centerY() + dy, rightEye.centerY() - movementRange, rightEye.centerY() + movementRange)

		new TranslateTransition
		{
			duration = Duration(500)
			node = leftPupil
			toX = newLeftX - leftEye.centerX()
			toY = newLeftY - leftEye.centerY()
		}.play()

		new TranslateTransition
		{
			duration = Duration(500)
			node = rightPupil
			toX = newRightX - rightEye.centerX()
			toY = newRightY - rightEye.centerY()
		}.play()
	}

	// Animation de clignement des yeux (les pupilles disparaissent)
	private def performBlink(): Unit =
	{
		leftPupil.visible = false
		rightPupil.visible = false

		leftEye.scaleY = 0.1
		rightEye.scaleY = 0.1
		rightEye.fill= Color.Black
		leftEye.fill = Color.Black
		new Timeline
		{
			keyFrames = Seq(
				KeyFrame(Duration(100), onFinished = _ =>
				{
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
	private def playRandomAnimation(): Unit =
	{
		Random.nextInt(5) match
		{
			case 0 => movePupils(0, -movementRange) // Regard en haut
			case 1 => movePupils(0, movementRange)  // Regard en bas
			case 2 => movePupils(-movementRange, 0) // Regard à gauche
			case 3 => movePupils(movementRange, 0)  // Regard à droite
			case 4 => performBlink()               // Clignement
		}
	}

	// Boucle des animations
	private val animationLoop: Timeline = new Timeline
	{
		cycleCount = Timeline.Indefinite
		keyFrames = Seq(
			KeyFrame(Duration(1000), onFinished = _ => playRandomAnimation())
		)
	}

	override def start(): Unit =
	{
		animationLoop.play()
		progressBarUpdater.play()

		stage = new PrimaryStage
		{
			title = "Virtual Assistant Eyes Animation"
			scene = new Scene(480, 320)
			{
				content = List(leftEye, rightEye, leftPupil, rightPupil, progressBarBackground, progressBar)
				fill = Color.LightBlue
				cursor = scalafx.scene.Cursor.None
			}
			fullScreen = true
		}
	}

	def setBackgroundColor(color: String): Unit =
	{
		color.toLowerCase match {
			case "blue" => stage.scene().fill = Color.Blue
			case "black" => stage.scene().fill = Color.Black
			case "green" => stage.scene().fill = Color.Green
			case "default" => stage.scene().fill = Color.LightBlue
			case _ => println(s"Unknown color: $color")
		}
	}
}
