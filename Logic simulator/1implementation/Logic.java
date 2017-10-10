    public static String nextType(Scanner sc) {
        sc.skip( whitespace );
        sc.skip( gateType );
        if (!"".equals( sc.match().group() )){
            return sc.match().group();
        }
        else {
            Errors.warn( "Gate type expected." );
            return "";
        }
    }
    /** Advance to next line and complain if is junk at the line end
     *  @see Errors
     *  @param sc the scanner from which end of line is scanned
     *  @param message gives a prefix to give context to error messages
     *  This version supports comments starting with --
     */
    public static void lineEnd( Scanner sc, String message ) {
        sc.skip( whitespace );
        String lineEnd = sc.nextLine();
        if ( (!lineEnd.equals( "" ))
        &&   (!lineEnd.startsWith( "--" )) ) {
            Errors.warn(
Type  :quit<Enter>  to exit Vim                               138,1-8       27%
