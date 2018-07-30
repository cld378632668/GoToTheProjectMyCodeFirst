package Util.HtmlCrawlerHelper;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import Util.HTML;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * getProxyIPS(String html) 获取一段html代码中所有的IPs
 *
 *
 */

public class ProxyIPGetter {

    /**
     * excute()是类的主函数，主要逻辑：
     * allIPs = getAllProxyIPs(ipLibURL) 从代理ip网站首页网址获取所有代理IP
     * validIPs = getValidProxyIPs(allIPs） 从所有代理IP中获取有效代理Ip
     * iPsSupportedWeibo = new ProxyIP()
     * 				.filterIPsSupportSpeWebsite(validIPs, plainIPsPath, weiboUrl); //过滤所有支持微博的代理IP
     *
     * @param args
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     */
    public void excute(String[] args)
            throws ClientProtocolException, IOException, URISyntaxException {
        long t1 = System.currentTimeMillis();
        // args[0] = savePlainIPs + "allIPs.txt";
        // args[1] = savePlainIPs + "validIPs.txt";
        // args[2] = savePlainIPs + "iPsSupportedWeibo.txt";
        String allIPsPath = args[0];
        String validIPsPath = args[1];
        String plainIPsPath = args[2];
        String ipLibURL = "http://www.xici.net.co/"; //此网站已失效，该网站未失效
        Vector<String> validIPs = new Vector<String>();
        Vector<String> allIPs = new Vector<String>();
        Vector<String> iPsSupportedWeibo = new Vector<String>();
        allIPs = getAllUnverifiedProxyIPs(ipLibURL);
        FileOperation.write2txt(allIPs, allIPsPath);
        validIPs = getValidProxyIPs(allIPs);
        FileOperation.write2txt(validIPs, validIPsPath);
        iPsSupportedWeibo = new ProxyIPGetter()
                .filterIPsSupportSpeWebsite(validIPs, plainIPsPath,"http:");

        int plainIPsNum = iPsSupportedWeibo.size();
        for(int i = 0; i < iPsSupportedWeibo.size(); i++){
//            JTARunInfo.append(iPsSupportedWeibo.get(i)+"\r\n");
        }
        FileOperation.write2txt(iPsSupportedWeibo, plainIPsPath);
        long t2 = System.currentTimeMillis();
        System.out.println("获取可用IP耗时" + (double) (t2 - t1) / 60000 + "分钟");
//        JTARunInfo.append("获取可用IP耗时" + (double) (t2 - t1) / 60000 + "分钟"
//                + "\r\n");
    }


    /**
     *
     * 过滤出能用于连接特定网址的proxyIps
     *
     * @param IPs
     *            - the Vector<String> contains all valid IPs
     * @param outfilePath
     *
     *
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public Vector<String> filterIPsSupportSpeWebsite(Vector<String> IPs, String specifyUrl,
												String outfilePath)
            throws ClientProtocolException, IOException {
        final String verificationURL = "http://s.weibo.com/weibo/李雪山hakka&nodup=1&page=1";
        // Vector<String> utf8IPs = new Vector<String>();
        Vector<String> plainIPs = new Vector<String>();
        String ip;

        for (int i = 0; i < IPs.size(); i++) {
            System.out.println("****开始验证第"+(i+1)+"个validIP");
//            JTARunInfo.append("****开始验证第 "+(i+1)+" 个validIP"+"\r\n");
            ip = IPs.get(i);
            String html = new HTML().getHTMLbyProxy(verificationURL,
                    ip.split(":")[0], Integer.parseInt(ip.split(":")[1]));
            int iReconn = 0;
            int reConnectTimes = 5;
            while (html.equals("null")) {
                if (iReconn == (reConnectTimes-1)) {
                    System.out
                            .println("****连续"+reConnectTimes+"次连接微博搜索站点(http://s.weibo.com/weibo)失败，放弃此IP****");
//                    JTARunInfo
//                            .append("****连续"+reConnectTimes+"次连接微博搜索站点(http://s.weibo.com/weibo)失败，放弃此IP****"
//                                    + "\r\n");
                    break;
                }
                html = new HTML().getHTMLbyProxy(verificationURL,
                        ip.split(":")[0], Integer.parseInt(ip.split(":")[1]));
                iReconn++;
                System.out.println("****" + ip + "is reconnecting the"
                        + iReconn + " time****");
//                JTARunInfo.append("****" + ip + "重连第" + iReconn + " 次****"
//                        + "\r\n");

            }
            if (html.contains("version=2012")) {
                plainIPs.add(ip);
                System.out.println("第 "+(i+1)+" 个validIP是可用IP(plainIP): " + ip);
//                JTARunInfo.append("第 "+(i+1)+" 个validIP是可用IP(plainIP): " + ip + "\r\n");
                // write2txt(html, "d:/data/weibo/test/2012_"+i+".html");
            }
            else{
                if(html.contains("version=2014")){
                    System.out.println("第 "+(i+1)+" 个validIP: " + ip +"可用于2014版本的html，但不可用于2012版即不可用于此软件");
//                    JTARunInfo.append("第 "+(i+1)+" 个validIP: " + ip +"可用于2014版本的html，但不可用于2012版即不可用于此软件"+ "\r\n");
                }
                else{
                    System.out.println("第 "+(i+1)+" 个validIP: "+ip+" 无效（难以连接或不适用于2012及2014版的微博搜索站点）");
//                    JTARunInfo.append("第 "+(i+1)+" 个validIP: "+ip+" 无效（难以连接或不适用于2012及2014版的微博搜索站点）"+"\r\n");
                }
            }
        }

        return plainIPs;
    }



    /**
     * Test all proxy IPs and select the valid ones.
     *
     * @param allIPs String Vector which contains all proxy IPs
     * @return a String Vector which contains all valid IPs selected from all
     *         candidate proxy IPs
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static Vector<String> getValidProxyIPs(Vector<String> allIPs) throws ClientProtocolException, IOException {
        System.out.println("********start getting valid proxy IPs********");

        //Vector<String> validHostname = new Vector<String>();
        Vector<String> validHostWithPort = new Vector<String>();
        int validIPNum = 0;
        for (int i = 0; i < allIPs.size(); i++) {
            // if(i == 100){
            // break;
            // }
            String ip = allIPs.get(i);
            String hostWithPort = "null";
            String hostName = ip.split(":")[0];
            String portString = ip.split(":")[1];
            int port = Integer.parseInt(portString);
            String varifyURL = "http://ip.uee.cn/";// http://ip.uee.cn/
            // http://iframe.ip138.com/ic.asp

            String html = new HTML().getHTMLbyProxy(varifyURL, hostName, port);
            int iReconn = 0;
            int reConnectTimes = 2;//视网速而定
            while (html.equals("null")) {// reconnect 2 times (total 3 times
                // connection)
                if (iReconn == (reConnectTimes-1)) {
                    System.out.println(reConnectTimes+" 次连接超时，放弃此IP");
                    break;
                }
                System.out.println("****重新连接****");
                html = new HTML().getHTMLbyProxy(varifyURL, hostName, port);
                iReconn++;
            }
            Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
            Matcher m = p.matcher(html);
            //String s;
            if (m.find()) {
                Document doc = Jsoup.parse(html);
                Element ele = doc.select("center").first();
                String showIP = ele.text();
                System.out.println(showIP);
                String s = m.group();
                hostWithPort = s + ":" + portString;
//				if (!validHostname.contains(s)) {
//					validHostname.add(s);//
//					validIPs.add(s + ":" + String.valueOf(port));
//					// bw.write(s+"\r\n");//write a valid proxy ip
//					validIPNum++;
//					System.out.println("valid proxy IP " + s + ":"+ String.valueOf(port));
//					JTARunInfo.append("第 "+(i+1)+"条IP是可用代理IP " + s + ":"+ String.valueOf(port) + "\r\n");
//				}
                if (!validHostWithPort.contains(hostWithPort)) {
                    validHostWithPort.add(hostWithPort);//
                    //validIPs.add(s + ":" + String.valueOf(port));
                    // bw.write(s+"\r\n");//write a valid proxy ip
                    validIPNum++;
                    System.out.println("valid proxy IP " + hostWithPort);
                }
                else{
                    System.out.println("No."+(i+1)+" IP " + hostWithPort + "have been saved.");
//                    JTARunInfo.append("第 "+(i+1)+"条IP " + hostWithPort + "已存在" +"\r\n");
                }
            } else {
                System.out.println("No."+(i+1)+" IP is invalid.");
//                JTARunInfo.append("第 "+(i+1)+"条代理IP不可用" + "\r\n");
            }
            System.out.println("NO." + (i+1) + " ip"+ ip +" be verified");
//            JTARunInfo.append("第 " + (i+1) + " 条IP"+ ip +"可用性验证完毕，实际使用时ip为" +hostWithPort+ "\r\n");

        }
        System.out.println("total number of valid IPs " + validIPNum);
//        JTARunInfo.append("有效代理IP总数：  " + validIPNum + "\r\n");

        return validHostWithPort;
    }

    /**
     * Get all unverified proxy IP for url:ipLibURL.
     *
     * Vector<String> getAllUnverifiedProxyIPs(String ipLibURL)
     * @param ipLibURL 动态代理ip库的网址首页，用于获取所有子页面和爬取上面所有的IP
     * 依赖：
     *      Vector<String> IPsPageLinks = getIPsPageLinks(ipLibURL); //获取所有子页面的URL
     *      onePageIPs = getProxyIPs(html[1]);  根据一个子页面的Html代码解析出上面所有的代理ips
     * @return a String Vector contains all unverified IPs
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static Vector<String> getAllUnverifiedProxyIPs(String ipLibURL) throws ClientProtocolException, IOException,
            URISyntaxException {
        Vector<String> IPsPageLinks = getIPsPageLinks(ipLibURL);// "http://www.xici.net.co/"
        Vector<String> onePageIPs = new Vector<String>();
        Vector<String> allIPs = new Vector<String>();
        for (int i = 0; i < 1; i++) {
            String url = IPsPageLinks.get(i);
            String[] html = new HTML().getHTML(url);
            int iReconn1 = 0;
            int reConnectTimes1 = 5;
            while(html[1].equals("null")){
                if(iReconn1 == (reConnectTimes1-1)){
                    System.out.println("连续"+reConnectTimes1+"次连接失败，继续获取下一条IP库的代理IP");
//                    JTARunInfo.append("连续"+reConnectTimes1+"次连接失败，继续获取下一条IP库的代理IP"+"\r\n");
                    break;
                }
                System.out.println("****重新连接****");
//                JTARunInfo.append("****重新连接****"+"\r\n");
                html[1] = new HTML().getHTML(ipLibURL)[1];
                iReconn1++;
            }
            System.out.println("next");
            int page = 180;
            while (html[0].equals("200")) {
                System.out.println("start finding proxy IPs under this link: "
                        + url);
//                JTARunInfo.append("开始获取这条链接下的代理IP: " + url + "\r\n");
                // JTARunInfo.paintImmediately(JTARunInfo.getBounds());

                onePageIPs = getProxyIPs(html[1]);
                if(onePageIPs.size()==0)
                    break;
                for (int j = 0; j < onePageIPs.size(); j++) {
                    String s = onePageIPs.get(j);
                    if (!allIPs.contains(s)) {
                        allIPs.add(s);
                    }
                }

                url = url.substring(0,url.lastIndexOf("/")+1) + page;
                // System.out.println("page = "+page);
                html = new HTML().getHTML(url);
                int iReconn = 0;
                int reConnectTimes = 5;
                while(html[1].equals("null")){
                    if(iReconn == (reConnectTimes-1)){
                        System.out.println("连续"+reConnectTimes+"次连接失败，继续获取下一条IP库的代理IP");
                        break;
                    }
                    System.out.println("****重新链接****");
                    html = new HTML().getHTML(url);
                    iReconn++;
                }
                System.out.println("状态码 "+html[0]);
                page++;
            }
        }
        System.out.println("total proxy IP number:： " + allIPs.size());

        return allIPs;
    }

    /**
     * Find all IP library links on the homepage of "http://www.xici.net.co/". 网址失效换一个
     *
     * @param ipLibURL
     *            specified URL "http://www.youdaili.cn/"
     * @return a String Vector contains all URLs that contain some proxy IPs
     * @throws ClientProtocolException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static Vector<String> getIPsPageLinks(String ipLibURL
                                                ) throws ClientProtocolException,
            URISyntaxException, IOException {
        Vector<String> IPsPageLinks = new Vector<String>();
        String html = new HTML().getHTML(ipLibURL)[1];
        while(html.equals("null")){
            System.out.println("****重新连接****");
//            JTARunInfo.append("****重新连接****"+"\r\n");
            html = new HTML().getHTML(ipLibURL)[1];
        }
        Pattern p = Pattern.compile("li.+([国内]|[国外]).+?li");// "【国内】|【国外】).+?title"
        Matcher m = p.matcher(html);
        String s;
        while (m.find()) {
            s = m.group();

            s = "http://www.xici.net.co"+s.substring(s.indexOf("href") + 6, s.indexOf("class") - 2);
            System.out.println(s+"\n");
            IPsPageLinks.add(s);
            System.out.println("find ip library link: " + s);
//            JTARunInfo.append("找到一条代理IP库链接: " + s + "\r\n");
        }

        return IPsPageLinks;
    }


    /**
     * 获取一个HTML页面中所有的IPs
     *
     * There are proxy ips which haven't been verified on the page like  ??
     * http://www.youdaili.cn/Daili/guonei/1843.html, use regex to match all
     * them out.
     *
     * @param html
     *              html code
     * @return a String Vector contains all the IP on the html file
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static Vector<String> getProxyIPs(String html)
            throws ClientProtocolException, IOException {
        Vector<String> IPs = new Vector<String>();
        Pattern p = Pattern
                .compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}</td>\n.+<td>\\d{1,5}</td>");
        Matcher m = p.matcher(html);
        String s;
        String port;
        while (m.find()) {
            s = m.group();
            s=s.substring(0,s.indexOf("</td>"))+":"+s.substring(s.indexOf("<td>")+4,s.lastIndexOf("</td>"));
            System.out.println(s);
            port = s.split(":")[1];
            if (Integer.parseInt(port) < 65535) {// The top range of the port
                // number is 65535
                if (!IPs.contains(s)) {
                    IPs.add(s);
                }
            }
            // System.out.println("找到一条ip "+s);

        }

        return IPs;
    }







}
