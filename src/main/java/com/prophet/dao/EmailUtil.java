package com.prophet.dao;

import java.io.File;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Repository;

@Repository
public class EmailUtil {
	@Autowired
	private JavaMailSender mailSender;		//在引入spring-boot-starter-mail依赖后会根据配置文件中的内容创建JavaMailSender实例
	@Value("${spring.mail.from}")
	private String mailFrom;				//发件人
	@Value("${spring.mail.company.suffix}")
	private String companyEmailSuffix;		//公司的邮箱统一
	
	/**
	 * 发送不带附件的邮件，只有标题和正文
	 * @param mailTo
	 * @param subject
	 * @param text
	 * @throws Exception
	 */
	public void sendSimpleMail(String mailTo, String subject, String text) throws Exception {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(this.mailFrom);
		message.setTo(mailTo + this.companyEmailSuffix);
		message.setSubject(subject);
		message.setText(text);

		mailSender.send(message);
	}
	
	/**
	 * 发送可以带一个或多个附件的邮件，文件名需要是绝对路径
	 * @param mailTo
	 * @param subject
	 * @param text
	 * @param attachments
	 * @throws Exception
	 */
	public void sendAttachmentsMail(String mailTo, String subject, String text, String ... attachments) throws Exception {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		helper.setFrom(this.mailFrom);
		helper.setTo(mailTo + this.companyEmailSuffix);
		helper.setSubject(subject);
		helper.setText(text);

		for (int i = 0; i < attachments.length; i++) {
			File file = new File(attachments[i]);
			
			FileSystemResource attachment = new FileSystemResource(file);
			helper.addAttachment(file.getName(), attachment);
		}
		
		mailSender.send(mimeMessage);
	}
}
