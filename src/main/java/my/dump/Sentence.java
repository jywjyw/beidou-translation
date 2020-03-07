package my.dump;

import java.util.ArrayList;
import java.util.List;

public class Sentence {
	public String text;
	public int index,len;
	public List<Addr> addrs=new ArrayList<>();
	public Sentence(String text, int len) {
		this.text=text;
		this.len=len;
	}

}

class Addr{
	public String file;
	public int offset;
	public Addr(String file, int offset) {
		this.file = file;
		this.offset = offset;
	}
}
