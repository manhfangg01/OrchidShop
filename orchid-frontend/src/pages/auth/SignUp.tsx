import { useEffect, useRef, useState } from "react";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { Link, useNavigate } from "react-router-dom";
import { Bounce } from "react-toastify";
import { toast } from "react-toastify";
import { authApi } from "../../services/auth/AuthApi";
import { Utils } from "../../utils/Utils";

interface FormErrors {
  username?: string;
  password?: string;
  fullname?: string;
  confirmPassword?: string;
}

const SignUp = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: "",
    fullname: "",
    password: "",
    confirmPassword: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [errors, setErrors] = useState<FormErrors>({});
  const [isLoading, setIsLoading] = useState(false);
  const fullNameInputRef = useRef<HTMLInputElement>(null);
  const usernameInputRef = useRef<HTMLInputElement>(null);
  const passwordInputRef = useRef<HTMLInputElement>(null);
  const confirmPasswordInputRef = useRef<HTMLInputElement>(null);

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

  useEffect(() => {
    fullNameInputRef.current?.focus();
    // toast.success("Test toast!");
  }, []);

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

    if (!formData.fullname.trim()) {
      newErrors.fullname = "Tên tài khoản là bắt buộc";
    } else if (formData.fullname.trim().length < 6) {
      newErrors.fullname = "Tên tài khoản phải có ít nhất 6 kí tự";
    }

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

    if (!(formData.password === formData.confirmPassword) || !formData.confirmPassword.trim()) {
      newErrors.confirmPassword = "Xác nhận mật khẩu thất bại";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleOnSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // Trong handleOnSubmit, sau khi validate thất bại:
    if (!validateForm()) {
      // Focus vào ô đầu tiên có lỗi
      if (errors.fullname) {
        fullNameInputRef.current?.focus();
      } else if (errors.username) {
        usernameInputRef.current?.focus();
      } else if (errors.password) {
        passwordInputRef.current?.focus();
      } else if (errors.confirmPassword) {
        confirmPasswordInputRef.current?.focus();
      }
    }
    // ✅ Tạo req từ formData, bỏ confirmPassword
    const req = {
      username: formData.username.trim(),
      password: formData.password,
      fullName: formData.fullname.trim(), // lưu ý: API dùng fullName, formData là fullname
    };
    setIsLoading(true);

    try {
      const response = await authApi.callSignUp(req); // 👈 await để lấy data
      console.log(response);
      // Đăng nhập thành công
      showToast("success", "Đăng kí thành công!");

      // Chuyển hướng (tùy logic app: về dashboard, home, hoặc redirect URL)
      navigate("/login"); // hoặc "/"
    } catch (err: any) {
      console.error("Login error:", err);

      let message = "Đăng kí thất bại. Vui lòng thử lại.";

      // Xử lý lỗi từ server (giả sử API trả về { message: "..." } hoặc lỗi validation)
      if (err.response) {
        const status = err.response.status;
        const serverMessage = err.response.data?.message;

        if (status === 401) {
          message = serverMessage || message;
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
      <div className="max-w-md w-full space-y-7">
        {/*Header */}
        <div className="text-center">
          <h1 className="text-3xl font-extrabold text-gray-900"> Đăng kí</h1>
        </div>
        {/*Form */}
        <div className="shadow-lg rounded-xl p-8">
          <form className="space-y-5" onSubmit={handleOnSubmit}>
            {/*fullname zone */}
            <div>
              <label htmlFor="fullname" className="block text-xl font-medium text-gray-700 ">
                Tên tài khoản
              </label>
              <div className="mt-1">
                <input
                  ref={fullNameInputRef} // Thêm focus đây
                  id="fullname"
                  name="fullname"
                  type="text"
                  placeholder="Nhập tên tài khoản"
                  value={formData.fullname}
                  onChange={handleOnInputChange}
                  autoComplete="name"
                  className={`w-full p-4 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${errors.fullname ? "border-red-500" : "border-gray-300"}`}
                />
                {errors.fullname && <p className="mt-1 text-sm text-red-600">{errors.fullname}</p>}
              </div>
            </div>
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
                  autoComplete="username"
                  className={`w-full p-4 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${errors.username ? "border-red-500" : "border-gray-300"}`}
                />
                {errors.username && <p className="mt-1 text-sm text-red-600">{errors.username}</p>}
              </div>
            </div>

            {/*Password zone */}
            <div className="my-8">
              <label htmlFor="password" className="block text-xl font-medium text-gray-700 ">
                Mật khẩu
              </label>
              <div className="mt-1 relative">
                <input
                  ref={passwordInputRef} // Thêm focus đây
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="Nhập mật khẩu của bạn"
                  value={formData.password}
                  onChange={handleOnInputChange}
                  autoComplete="new-password"
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

            {/*ConfirmPassword zone */}
            <div className="my-8">
              <label htmlFor="confirmPassword" className="block text-xl font-medium text-gray-700 ">
                Xác nhận mật khẩu
              </label>
              <div className="mt-1 relative">
                <input
                  ref={confirmPasswordInputRef} // Thêm focus đây
                  id="confirmPassword"
                  name="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  placeholder="Nhập lại mật khẩu"
                  value={formData.confirmPassword}
                  onChange={handleOnInputChange}
                  autoComplete="new-password"
                  className={`w-full p-4 border rounded-lg outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 ${errors.confirmPassword ? "border-red-500" : "border-gray-300"}`}
                />
                {formData.confirmPassword && (
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute inset-y-0 right-3 flex items-center text-gray-500 hover:text-gray-700 focus:outline-none font-lg"
                  >
                    {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
                  </button>
                )}
              </div>
              {errors.confirmPassword && <p className="mt-1 text-sm text-red-600">{errors.confirmPassword}</p>}
            </div>
            {/* Đăng kí ngay */}
            <div className="flex justify-center">
              <div className="text-center">
                <p className="text-lg text-gray-500">Đã có tài khoản ?</p>
                <Link to="/login" className="text-lg text-blue-400 hover:cursor-pointer hover:underline hover:text-blue-700">
                  Đăng nhập ngay !
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
                {isLoading ? "Đang xử lý" : "Đăng kí"}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
export default SignUp;
