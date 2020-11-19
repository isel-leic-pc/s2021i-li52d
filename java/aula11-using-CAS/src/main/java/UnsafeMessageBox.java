public class UnsafeMessageBox<M>   {
    private class MsgHolder {
        public  M msg;
        public int lives;

        public MsgHolder(M msg, int lives) {
            this.msg = msg;
            this.lives = lives;
        }
    }

    private  MsgHolder msgHolder = null;

    public void Publish(M m, int lvs) {

        msgHolder = new MsgHolder(m, lvs);
    }

    public M TryConsume() {
        if (msgHolder != null && msgHolder.lives > 0) {
            msgHolder.lives -= 1;
            return msgHolder.msg;
        }
        return null;
    }
}
