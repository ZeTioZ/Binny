package be.unamur.binny.animation

import scalafx.animation.{KeyFrame, Timeline}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

import scalafx.util.Duration
import scalafx.Includes.*

class VirtualAssistantEyes extends JFXApp3 {

	val eyeRadius = 45.0   // Taille des yeux
	val pupilRadius = 15.0 // Taille des pupilles
	val movementRange = 9.5  // Limite de déplacement des pupilles

	// Création des yeux (les grands cercles blancs)
	val leftEye = new Circle {
		centerX = 150
		centerY = 100
		radius = eyeRadius
		fill = Color.White
		stroke = Color.Black
		strokeWidth = 2
	}

	val rightEye = new Circle {
		centerX = 250
		centerY = 100
		radius = eyeRadius
		fill = Color.White
		stroke = Color.Black
		strokeWidth = 2
	}

	// Création des pupilles (les petits cercles noirs)
	val leftPupil = new Circle {
		centerX = leftEye.centerX()
		centerY = leftEye.centerY()
		radius = pupilRadius
		fill = Color.Black
	}

	val rightPupil = new Circle {
		centerX = rightEye.centerX()
		centerY = rightEye.centerY()
		radius = pupilRadius
		fill = Color.Black
	}

	// Animation qui déplace les pupilles de gauche à droite
	val eyeMovementTimeline = new Timeline {
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

	override def start(): Unit = {
		// Démarrage de l'animation au lancement
		eyeMovementTimeline.play()

		stage = new PrimaryStage {
			title = "Virtual Assistant Eyes Animation"
			scene = new Scene(400, 200) {
				content = List(leftEye, rightEye, leftPupil, rightPupil)
				fill = Color.LightBlue
			}
		}
	}
}