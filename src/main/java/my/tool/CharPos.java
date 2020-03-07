package my.tool;

public class CharPos {
	public static void main(String[] args) {
		int x=3,y=10;	//x and y <=21
		System.out.println(Integer.toHexString(y<<5|x));
		int code=0x143;
		int u=(code&0x1f) * 12;
		int v=((code&0x3e0)>>>5) * 16;	//TODO 
		System.out.printf("u=%x,v=%x\n",u,v);
	}

}
