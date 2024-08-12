package me.everything.android.ui.overscroll

interface ListenerStubs {
    class OverScrollStateListenerStub : IOverScrollStateListener {
        override fun onOverScrollStateChange(
            decor: IOverScrollDecor?,
            oldState: Int,
            newState: Int
        ) {
        }
    }

    class OverScrollUpdateListenerStub : IOverScrollUpdateListener {
        override fun onOverScrollUpdate(decor: IOverScrollDecor?, state: Int, offset: Float) {}
    }
}
