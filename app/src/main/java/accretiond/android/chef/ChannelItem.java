package accretiond.android.chef;

/**
 * Help item
 * 
 * <p>
 * Properties: String tx_question
 * </p>
 *
 */

public class ChannelItem {

	private String tx_chan;
	private int chan_id;


	public ChannelItem(String tx_question, int id)
	{
		this.tx_chan = tx_question;
		this.chan_id=id;
	}
	
	public String getQuestion()
	{
		return tx_chan;
	}

	public int getChan_id() {
		return chan_id;
	}

	public void setChan_id(int chan_id) {
		this.chan_id = chan_id;
	}

	public void setTx_chan(String tx_chan) {
		this.tx_chan = tx_chan;
	}
}
