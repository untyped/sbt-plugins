import javax.servlet.http._

class HelloWorld extends HttpServlet {

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse) =
    resp.getWriter.print("Run mode is " + Props.mode)

}
