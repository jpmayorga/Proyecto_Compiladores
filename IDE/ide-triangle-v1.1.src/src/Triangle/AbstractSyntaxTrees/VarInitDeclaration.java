/*
 * David
 */
package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class VarInitDeclaration extends Declaration {

  public VarInitDeclaration (Identifier iAST, Expression eAST,
                         SourcePosition thePosition) {
    super (thePosition);
    I = iAST;
    E = eAST;
  }

    /**
     *
     * @param v
     * @param o
     * @return
     */
  public Object visit(Visitor v, Object o) {
    return v.visitVarInitDeclaration(this, o);
  }
  public Identifier getIdentifier (){
      return I;
  }
  public Identifier I;
  public Expression E;
}
