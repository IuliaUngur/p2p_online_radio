
public class StartPrototype {

	public static void main(String[] args){
		
		if(args.length != 1){
			System.out.println("You provided a wrong amount of arguments!");
			System.exit(1);
		}
		
		new P2PCLI(Integer.valueOf(args[0]));
		
	}
	
}
