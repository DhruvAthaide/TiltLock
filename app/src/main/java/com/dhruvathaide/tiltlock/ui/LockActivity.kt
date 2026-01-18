package com.dhruvathaide.tiltlock.ui

class LockActivity : AppCompatActivity() {

    private lateinit var vm: TiltLockViewModel
    private lateinit var gyro: GyroManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock)

        val repo = GestureRepository(this)
        vm = ViewModelProvider(
            this,
            ViewModelProvider.Factory {
                TiltLockViewModel(repo)
            }
        )[TiltLockViewModel::class.java]

        gyro = GyroManager(this) { x, y ->
            vm.onGyro(x, y)
            applyParallax(x, y)
        }

        observeState()
    }

    private fun observeState() {
        vm.lockState.observe(this) {
            when (it) {
                LockState.SUCCESS -> glow(Color.CYAN)
                LockState.ERROR -> shake()
                else -> resetGlow()
            }
        }

        vm.progress.observe(this) {
            performHapticFeedback()
        }
    }

    private fun applyParallax(x: Float, y: Float) {
        findViewById<View>(R.id.vaultCard).apply {
            rotationX = x * 4
            rotationY = y * 4
        }
    }

    private fun shake() {
        val anim = ObjectAnimator.ofFloat(
            findViewById(R.id.vaultCard),
            "translationX",
            0f, -20f, 20f, -10f, 0f
        )
        anim.duration = 500
        anim.start()
    }

    private fun glow(color: Int) {
        findViewById<View>(R.id.iris)
            .background.setTint(color)
    }

    private fun resetGlow() {
        findViewById<View>(R.id.iris)
            .background.setTint(Color.GRAY)
    }

    private fun performHapticFeedback() {
        getSystemService(Vibrator::class.java)
            ?.vibrate(VibrationEffect.createOneShot(40, 100))
    }

    override fun onResume() {
        super.onResume()
        gyro.start()
        vm.reloadGesture()
    }

    override fun onPause() {
        super.onPause()
        gyro.stop()
    }
}
