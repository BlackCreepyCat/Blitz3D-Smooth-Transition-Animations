Graphics3D(1024,768,32,2)
SetBuffer(BackBuffer())
SeedRnd(MilliSecs())

Global frameTimer=CreateTimer(60)

;You don't need this variable in your code, but it's useful here because we are adjusting the value dynamically

Global transitionMethod#=1.0

;We are going to use really two transitions, but I'm creating three.  One for pitch, one for yaw, and one for roll (which is ignored)

Global p.Transition,y.Transition,r.Transition

;This is just the stuff you see on screen

Local cam=CreateCamera()
MoveEntity(cam,0,0,-300)
CameraZoom(cam,150)
CameraRange(cam,200,400)
Local sun=CreateLight()
LightColor(sun,255,255,200)
RotateEntity(sun,45,45,0)
AmbientLight(150,150,200)

Local plane=CreatePlane()
RotateEntity(plane,-90,0,0)
PositionEntity(plane,0,0,5)
EntityColor(plane,32,32,32)
EntityOrder(plane,20)

Local clockHand=CreateSphere(20)
ScaleEntity(clockHand,.05,.05,.05)
PositionEntity(clockHand,0,1.05,0)
Local clockBase=CreatePivot()
EntityParent(clockHand,clockBase)

Local sphere=CreateSphere(32)
RotateEntity(sphere,0,180,0)
EntityOrder(sphere,10)
EntityColor(sphere,128,0,0)
FlipMesh(sphere)
Local tex=CreateTexture(32,32)
SetBuffer(TextureBuffer(tex))
ClsColor(255,255,255)

Cls
Color(0,0,0)
Rect(0,0,32,32,0)
Line(15,0,15,32)
Line(0,15,32,15)
EntityTexture(sphere,tex)
ScaleTexture(tex,.036,.072)
Color(255,255,255)
SetBuffer(BackBuffer())

Local thing=CreateCone(64)
RotateMesh(thing,90,0,0)
ScaleEntity(thing,.2,.2,1)

SetNewDirection(thing)

Local s=CreateSphere(16)
ScaleEntity(s,.2,.2,.2)
EntityColor(s,80,80,80)
EntityShininess(s,1)
EntityParent(s,thing)

Local c=CreateCylinder(16)
ScaleEntity(c,.1,.22,.1)
EntityColor(c,80,80,80)
EntityShininess(c,1)
RotateEntity(c,0,0,90)
EntityParent(c,thing)

Local c2=CreateCylinder()
ScaleEntity(c2,.02,.3,.02)
EntityColor(c2,80,80,80)
EntityShininess(c2,1)
RotateEntity(c2,0,0,90)
EntityParent(c2,c)

Local c3=CreateCube()
PositionMesh(c3,0,.97,0)
ScaleEntity(c3,.01,2,.05)
EntityColor(c3,80,80,80)
EntityShininess(c3,1)
PositionEntity(c3,-.29,0,0)

Local c4=CreateCube()
PositionMesh(c4,0,.97,0)
ScaleEntity(c4,.01,2,.05)
EntityColor(c4,80,80,80)
EntityShininess(c4,1)
PositionEntity(c4,.29,0,0)

Local piv=CreatePivot()
EntityParent(c3,piv)
EntityParent(c4,piv)

;Main loop.  You knew that, didn't you?
While Not KeyHit(1)
	
	WaitTimer(frameTimer)
	
	;If the transitions have expired, create new ones and purge the old ones (only expired transitions are purged.  This way there will always be a p.transition, y.transitoin and r.transition, which prevents errors.
	If CheckTransition(p.Transition)>0 
		SetNewDirection(thing)
		PurgeTransitions()
	EndIf
	
	;Here we use the value from GetTransitionValue()  to rotate the pointer and it's support structure
	RotateEntity(thing,GetTransitionValue(p.Transition),GetTransitionValue(y.Transition),GetTransitionValue(r.Transition),1)
	RotateEntity(piv,0,GetTransitionValue(y.Transition),0)
	
	RenderWorld()
	
	v1#=MilliSecs()-p\startTime
	v2#=p\endTime-p\startTime
	
	RotateEntity(clockBase,0,0,-360.0*v1/v2)
	
	Text(0,0,"Pitch="+GetTransitionValue(p.Transition))
	Text(0,20,"Yaw="+GetTransitionValue(y.Transition))
	Text(0,40,"Use arrow keys (Up/Down) to adjust transition type.  0.0=Linear, 1.0=Cosine "+transitionMethod)
	
	If KeyHit(200) Then transitionMethod=transitionMethod+0.1
	If KeyHit(208) Then transitionMethod=transitionMethod-0.1
	
	If transitionMethod>1.0 Then transitionMethod=1.0
	If transitionMethod<0.0 Then transitionMethod=0.0
	
	;Here we place a couple of visual markers for the values.
	Oval(GraphicsWidth()-GraphicsWidth()*(GetTransitionValue(y.Transition)+180.0)/360.0,GraphicsHeight()*.9,10,10,1)
	Oval(GraphicsWidth()*.05,GraphicsHeight()*(GetTransitionValue(p.Transition)+90.0)/180.0,10,10,1)
	
	Flip()
	Delay(10)
	
Wend

Function SetNewDirection(thing)
	
	Local ep#=EntityPitch(thing)
	Local ey#=EntityYaw(thing)
	Local er#=EntityRoll(thing)
	
	SeedRnd MilliSecs()
	Local newp#=Rnd(-80.0,180.0)
	Local newy#=Rnd(-16.0,100.0)
	Local newr#=Rnd(180.0,-50.0)
	
	;Here we create the transitions objects.  Only p and y are really used.
	
	p.Transition=CreateTransition(ep,newp,3000,transitionMethod)
	y.Transition=CreateTransition(ey,newy,3000,transitionMethod)
	r.Transition=CreateTransition(0,0,3000,transitionMethod)
	
	EntityColor(thing,Rnd(255),Rnd(255),Rnd(255))
	
End Function



Type Transition
	Field startValue#
	Field endValue#
	Field startTime#
	Field endTime#
	Field cosine#
End Type

Function CreateTransition.Transition(startValue#,endValue#,mSecs#,cosine#=1.0)
	Local t.Transition=New Transition
	t\startTime=MilliSecs()
	t\endTime=t\startTime+mSecs
	t\startValue=startValue
	t\endValue=endValue
	t\cosine=cosine
	Return t.Transition
End Function

Function GetTransitionValue#(t.Transition)
	If t.Transition=Null Then Return -9999
	Local straightProportion#=(MilliSecs()-t\startTime)/(t\endTime-t\startTime)
	If CheckTransition(t.Transition)>=0 Then straightProportion=1.0
	Local cosineProportion#=(1-Cos(straightProportion*180))*.5
	Local finalProportion#=cosineProportion*t\cosine+straightProportion*(1.0-t\cosine)
	Return finalProportion*(t\endValue-t\startValue)+t\startValue
End Function

Function PurgeTransitions(age=0)
	Local t.Transition
	For t.Transition=Each Transition
		If MilliSecs()>(t\endTime+age) Then Delete t.Transition
	Next
End Function

Function CheckTransition(t.Transition)
	Return MilliSecs()-t\endTime
End Function

;~IDEal Editor Parameters:
;~C#Blitz3D