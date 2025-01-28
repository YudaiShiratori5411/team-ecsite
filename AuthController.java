package jp.co.internous.team2411.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.team2411.model.domain.MstUser;
import jp.co.internous.team2411.model.form.UserForm;
import jp.co.internous.team2411.model.mapper.MstUserMapper;
import jp.co.internous.team2411.model.mapper.TblCartMapper;
import jp.co.internous.team2411.model.session.LoginSession;


/**
 * 認証に関する処理を行うコントローラー
 * @author インターノウス
 *
 */
@RestController
@RequestMapping("/team2411/auth")
public class AuthController {
	
    @Autowired
    private LoginSession loginSession;
	
	@Autowired
	private MstUserMapper userMapper;
	
	@Autowired
	private TblCartMapper cartMapper;
	
	private Gson gson = new Gson();
		
	/**
	 * ログイン処理をおこなう
	 * @param f ユーザーフォーム
	 * @return ログインしたユーザー情報(JSON形式)
	 */
	@PostMapping("/login")
	public String login(@RequestBody UserForm f) {

		MstUser user = userMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());
		
		if (user == null) {
	        loginSession.setUserId(0);
	        loginSession.setUserName(null);
	        loginSession.setPassword(null);
	        loginSession.setLogined(false);
			return "null";
		} 
		
		if (loginSession.getTmpUserId() != 0) {
			cartMapper.updateUserId(user.getId(), loginSession.getTmpUserId());
		}
	
	    loginSession.setUserId(user.getId());
	    loginSession.setTmpUserId(0);
	    loginSession.setUserName(user.getUserName());
	    loginSession.setPassword(user.getPassword());
	    loginSession.setLogined(true);

	    return gson.toJson(user);
	}
	
	/**
	 * ログアウト処理をおこなう
	 * @return 空文字
	 */
	@PostMapping("/logout")
	public String logout() {
		
		loginSession.setUserId(0);
		loginSession.setTmpUserId(0);
		loginSession.setUserName(null);
		loginSession.setPassword(null);
		loginSession.setLogined(false);
		
		return "";
	}

	/**
	 * パスワード再設定をおこなう
	 * @param f ユーザーフォーム
	 * @return 処理後のメッセージ
	 */
	@PostMapping("/resetPassword")
	public String resetPassword(@RequestBody UserForm f) {
		
		if (f.getPassword().equals(f.getNewPassword())) {
			return "現在のパスワードと同一文字列が入力されました。";
		}
		
		int updatedPassword = userMapper.updatePassword(f.getUserName(), f.getNewPassword());
		if (updatedPassword > 0) {
			loginSession.setPassword(f.getNewPassword());
			return "パスワードが再設定されました。";
		} else {
			return "パスワードの更新に失敗しました。";
		}
	}
}
