package dramamanager;

public class GameAdapter {

    public static GameAdapter instance;
    public boolean offerHint;
    
    public GameAdapter()
    {
        instance = this;
        offerHint = false;
    }
    
    public void Adapt(Evaluator evalFindings)
    {
        // this is based on the findings of the evaluation function
        //if(evalFindings.)
    }
    
    public void ProvideHint(String hintText)
    {
        
    }
    

}