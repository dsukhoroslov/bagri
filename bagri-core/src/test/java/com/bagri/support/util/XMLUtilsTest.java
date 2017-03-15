package com.bagri.support.util;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class XMLUtilsTest {

	private static String xml = "<map>\n" +
	"   <field1>8\\c/680Fe3Z!%.\"!,&gt;3Bg2V'3Q5 Ky'&gt;.)G'(1(5,b'X#$$x#S+(\"b16t'*|3!8=0p%Z&#x7f;&amp;':10v'L&#x7f;-W1-Mk1Vw25p52`&amp;T%%7f2</field1>\n" +
	"   <field0>3;((86)*(9[q-+,)&amp;x,P!4W!*Du+9:&gt;T%)/&gt;$:|&gt;Uu&lt;!t.$|&lt;Fg6N78Y{7F;$7r'?v5O5)Oa-4l/!~#&amp;*=Xm$^/ \"l(@y7]}([m&amp;</field0>\n" +
	"   <field7>6B==5b58l7G'&lt;Ba6,t;\"`7H-86v?N5&lt;Rm&gt;D9/7t&gt;S?%!x&gt;W??&lt;p6;&lt; Z-&lt;U/;Xg?6d-*:=&amp;*9*p&gt;_&#x7f;$Ee*N1:Hw#1h5:v2:6/)21</field7>\n" +
	"   <field6>8'|&amp;Tc,&gt;j/Dm/Y+ 9&lt;'!. !p,N514`75&lt;-*v0W}3N})62&lt;B5&amp;E&#x7f;&gt;&amp;&amp;.Bw?!r#Fa.%$%8:,#&gt;4(44[55#l#]#?Hc*O53\"~:.t#  1</field6>\n" +
	"   <field9>$:4(?:&lt;!.&lt;56&lt;P;$Na2\"*$@g+@;(Am\"Vu&amp;@78B#!B3\"%`.&amp;b5$&gt;8Cu#N+0Ze7S91Dy/Sa\"\\}1%t*(0+O#(\\9$D&#x7f;67v$3\"5$,-O79</field9>\n" +
	"   <field8>6/*0_+(40,%2*O+0/j$=\"4?|!#8 /v,1.%M7!P)90$;9l\"J'$C;41,!5`\"Sq3Zu**r'%\"9T+&gt;0,5^o$6`3:t&amp;\\'8Va192 Jw20f*</field8>\n" +
	"   <field3>&lt;+$9X91^w7He=S/5\\q1^k'!l&amp;#p30h1F'5L!(^+8Z&#x7f;9_}7O)4E+=$t=\\q*H%0;b'B}=*l&gt;Ka22~.487@!-Gy9U1 E&#x7f;%L=!Jm)Jw2</field3>\n" +
	"   <field2>%$`&amp;/f-.(%Fy8B;%_?(Ku#Yy?2l6M7$Pa?9|7Xa7A&#x7f; !`70&amp;*Cc72:&amp;Ca;&lt;*-T=1%d/Ei'7z%0`#Q?-#.))v$$4!@w!Tw3 b\"&gt;:9</field2>\n" +
	"   <field5>.8d';&amp;;#&amp; 1&amp;2.b+*f8)(5(::/4-(z5\\'-^e7T5%:l6@7&lt;92 Yi&amp;H93^{'40&gt;Li'=|;#n&gt;\\m6Cu&amp;2&amp;%Tm6$h4?$=;&amp;%Ty6Zm9_o+</field5>\n" +
	"   <key>user8762246134350092690</key>\n" +
	"   <field4>=/8-+(&amp;$61S&#x7f;()89Hg2?0!Jk&amp;Ty7+t* v; p R/$0v9[o*6,4Tw/:`?8d#C%(?z7&amp;b&gt;9j6Qg&lt;1&gt;(4t25t2\"r,X{&gt;I#'T-8)&gt;&amp;/|;</field4>\n" +
	"</map>";	
	
	@Test
	public void testMapFromXML() throws Exception {
		Map map = XMLUtils.mapFromXML(xml);
		assertNotNull(map);
		assertEquals(11, map.size());
		assertEquals("user8762246134350092690", map.get("key"));
		for (int i=0; i < 10; i++) {
			String key = "field" + i;
			assertNotNull(map.get(key));
		}
	}
	
}
