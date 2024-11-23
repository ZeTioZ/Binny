package be.unamur.binny

class SharedState
{
	@volatile var isLidOpen: Boolean = false
	@volatile var lidDistance: Double = 0.0
	@volatile var isTouched: Boolean = false
	@volatile var isNear: Boolean = false
	@volatile var servoAngle: Double = 0.0
}