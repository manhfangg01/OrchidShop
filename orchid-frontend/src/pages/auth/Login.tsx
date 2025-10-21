import React, { useEffect, useRef, useState } from "react";
import { Bounce, toast } from "react-toastify";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { authApi } from "../../services/auth/AuthApi";
import { Link, useNavigate } from "react-router-dom";
import "react-toastify/dist/ReactToastify.css";
import { Utils } from "../../utils/Utils";

interface FormErrors {
  username?: string;
  password?: string;
}
const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<FormErrors>({});
  const [isLoading, setIsLoading] = useState(false);
  const usernameInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);

  type toastType = "success" | "warning" | "error" | "info";
  const showToast = (type: toastType, message: string) => {
    toast[type](message, {
      position: "top-center",
      autoClose: 5000,
      hideProgressBar: false,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
      theme: "light",
      transition: Bounce,
    });
  };

  // Tự động focus vào ô username khi vào trang
  useEffect(() => {
    usernameInputRef.current?.focus();
  }, []);

  //reset form khi vào trang
  // useEffect(() => {
  //   setFormData({ username: "", password: "" });
  //   setErrors({});
  // }, []);

  // // Khôi phục username từ sessionStorage (nếu có)
  // useEffect(() => {
  //   const savedUsername = sessionStorage.getItem("login-username");
  //   if (savedUsername) {
  //     setFormData((prev) => ({ ...prev, username: savedUsername }));
  //   }
  // }, []);

  const handleOnInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Xóa lỗi của field đó khi người dùng bắt đầu gõ
    if (errors[name as keyof FormErrors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {}; // ← rỗng

    if (!formData.username.trim()) {
      newErrors.username = "* Email hoặc tên đăng nhập là bắt buộc";
    } else if (!Utils.isValidEmail(formData.username)) {
      newErrors.username = "* Email không đúng định dạng";
    }
    if (!formData.password) {
      newErrors.password = "* Mật khẩu là bắt buộc";
    } else if (formData.password.length < 6) {
      newErrors.password = "* Mật khẩu phải có ít nhất 6 ký tự";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleOnSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Trong handleOnSubmit, sau khi validate thất bại:
    if (!validateForm()) {
      // Focus vào ô đầu tiên có lỗi
      if (errors.username) {
        usernameInputRef.current?.focus();
      } else if (errors.password) {
        passwordInputRef.current?.focus();
      }
      return;
    }
    setIsLoading(true);
    setErrors({}); // Xóa lỗi trước khi gọi API

    try {
      const response = await authApi.callLogin(formData); // 👈 await để lấy data
      console.log(response);
      // Đăng nhập thành công
      showToast("success", "Đăng nhập thành công!");

      // Chuyển hướng (tùy logic app: về dashboard, home, hoặc redirect URL)
      navigate("/"); // hoặc "/"
    } catch (err: any) {
      console.error("Login error:", err);

      let message = "Đăng nhập thất bại. Vui lòng thử lại.";

      // Xử lý lỗi từ server (giả sử API trả về { message: "..." } hoặc lỗi validation)
      if (err.response) {
        const status = err.response.status;
        const serverMessage = err.response.data?.message;

        if (status === 401) {
          message = "Email hoặc mật khẩu không đúng.";
        } else if (status === 400) {
          message = serverMessage || "Dữ liệu không hợp lệ.";
        } else if (status >= 500) {
          message = "Lỗi máy chủ. Vui lòng thử lại sau.";
        }
      }

      showToast("error", message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center">
          <h2 className="text-3xl font-extrabold text-gray-900">Đăng nhập</h2>
        </div>
        <div className="shadow-lg bg-white rounded-xl p-8">
          <form className="space-y-5" autoComplete="off" onSubmit={handleOnSubmit}>
            {/*Username zone */}
            <div>
              <label htmlFor="username" className="block text-xl font-medium text-gray-700 ">
                Tên đăng nhập hoặc Email
              </label>
              <div className="mt-1">
                <input
                  ref={usernameInputRef} // Thêm focus đây
                  id="username"
                  name="username"
                  type="text"
                  placeholder="Nhập email hoặc tên đăng nhập của bạn"
                  value={formData.username}
                  onChange={handleOnInputChange}
                  autoComplete="username" // Sau khi refresh vẫn giữ lại trường input này
                  className={`w-full p-4 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${errors.username ? "border-red-500" : "border-gray-300"}`}
                />
              </div>
              {errors.username && <p className="mt-1 text-sm text-red-600">{errors.username}</p>}
            </div>
            {/*Password zone */}
            <div className="my-8">
              <label htmlFor="password" className="block text-xl font-medium text-gray-700 ">
                Mật khẩu
              </label>
              <div className="mt-1 relative">
                <input
                  ref={passwordInputRef}
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="Nhập mật khẩu của bạn"
                  value={formData.password}
                  onChange={handleOnInputChange}
                  autoComplete="new-password" // Giúp sau khi refresh thì sẽ làm trống 2 input
                  className={`w-full p-4 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${errors.password ? "border-red-500" : "border-gray-300"}`}
                />
                {formData.password && (
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-3 flex items-center text-gray-500 hover:text-gray-700 focus:outline-none font-lg"
                  >
                    {showPassword ? <FaEyeSlash /> : <FaEye />}
                  </button>
                )}
              </div>
              {errors.password && <p className="mt-1 text-sm text-red-600">{errors.password}</p>}
            </div>

            <div className="flex justify-between">
              {/*Quên mật khẩu */}
              <div className="flex justify-end">
                <Link to="/forgot-password" className="text-blue-500 hover:cursor-pointer hover:underline text-medium">
                  Quên mật khẩu ?
                </Link>
              </div>

              {/*Đăng kí ngay*/}
              <div className="text-center">
                <p className="text-medium text-gray-500">Chưa có tài khoản ?</p>
                <Link to="/signup" className="text-medium text-blue-300 hover:cursor-pointer hover:underline hover:text-blue-500">
                  Đăng kí ngay !
                </Link>
              </div>
            </div>

            {/*Submit button */}
            <div>
              <button
                type="submit"
                disabled={isLoading}
                className={`w-full p-4 rounded-lg text-white font-medium outline-none
  ${isLoading ? "bg-purple-400 cursor-not-allowed" : "bg-purple-600 hover:bg-purple-700"}`}
              >
                {isLoading ? "Đăng xử lý" : "Đăng nhập"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
export default Login;
