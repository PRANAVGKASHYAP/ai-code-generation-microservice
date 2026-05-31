import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { api, isAuthenticated, getUserInfo, removeAuthToken, removeUserInfo } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Sparkles,
  Code,
  Check,
  ArrowRight,
  Terminal,
  Cpu,
  Globe,
  Crown,
  LogOut,
  CreditCard,
  Layers,
  Zap,
} from "lucide-react";

export default function Index() {
  const navigate = useNavigate();
  const [isAuth, setIsAuth] = useState(false);
  const [userName, setUserName] = useState("");
  const [userEmail, setUserEmail] = useState("");

  useEffect(() => {
    const authStatus = isAuthenticated();
    setIsAuth(authStatus);
    if (authStatus) {
      const userInfo = getUserInfo();
      if (userInfo) {
        setUserName(userInfo.name);
        setUserEmail(userInfo.username);
      }
    }
  }, []);

  const handleLogout = () => {
    removeAuthToken();
    removeUserInfo();
    setIsAuth(false);
    navigate("/");
  };

  const handleGetStarted = () => {
    if (isAuth) {
      navigate("/projects");
    } else {
      navigate("/signup");
    }
  };

  const handlePricingClick = (planId: number) => {
    if (isAuth) {
      navigate("/settings");
    } else {
      navigate("/signup");
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground relative overflow-hidden font-sans">
      {/* Background glow meshes */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none z-0">
        <div className="absolute top-[-10%] left-[-10%] w-[800px] h-[800px] bg-primary/5 rounded-full blur-3xl opacity-60" />
        <div className="absolute top-[20%] right-[-10%] w-[600px] h-[600px] bg-indigo-500/5 rounded-full blur-3xl opacity-40" />
        <div className="absolute bottom-[10%] left-[20%] w-[700px] h-[700px] bg-primary/5 rounded-full blur-3xl opacity-50" />
      </div>

      {/* Navigation Header */}
      <header className="border-b border-border/40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 sticky top-0 z-50 transition-all">
        <div className="container max-w-screen-2xl flex h-16 items-center justify-between px-4 sm:px-8">
          <div className="flex items-center gap-2.5 font-bold text-lg cursor-pointer" onClick={() => navigate("/")}>
            <div className="w-9 h-9 rounded-lg bg-primary/20 flex items-center justify-center border border-primary/20 shadow-md">
              <Sparkles className="w-5 h-5 text-primary" />
            </div>
            <span className="tracking-tight bg-gradient-to-r from-foreground to-foreground/80 bg-clip-text text-transparent">
              Project Companion
            </span>
          </div>

          {/* Center Links */}
          <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-muted-foreground">
            <button
              onClick={() => document.getElementById("features")?.scrollIntoView({ behavior: "smooth" })}
              className="hover:text-foreground transition-colors"
            >
              Features
            </button>
            <button
              onClick={() => document.getElementById("pricing")?.scrollIntoView({ behavior: "smooth" })}
              className="hover:text-foreground transition-colors"
            >
              Pricing
            </button>
            <a href="https://github.com" target="_blank" rel="noreferrer" className="hover:text-foreground transition-colors">
              Docs
            </a>
          </nav>

          {/* Right Action buttons */}
          <div className="flex items-center gap-4">
            {isAuth ? (
              <>
                <Button
                  onClick={() => navigate("/projects")}
                  className="hidden sm:inline-flex bg-primary hover:bg-primary/95 text-primary-foreground font-semibold shadow-md gap-1.5 rounded-full px-5 h-9 text-xs transition-transform hover:scale-[1.02]"
                >
                  Go to Workspace
                  <ArrowRight className="w-3.5 h-3.5" />
                </Button>

                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <button className="h-9 w-9 rounded-full border border-border/60 overflow-hidden hover:opacity-85 transition-opacity relative z-10 shrink-0">
                      <Avatar className="h-9 w-9">
                        <AvatarFallback className="bg-primary/10 text-primary font-bold text-sm">
                          {userName ? userName.charAt(0).toUpperCase() : "U"}
                        </AvatarFallback>
                      </Avatar>
                    </button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="w-56 mt-2 relative z-[60]">
                    <div className="flex flex-col space-y-1 p-2">
                      <p className="text-sm font-semibold leading-none">{userName || "User"}</p>
                      <p className="text-xs leading-none text-muted-foreground">{userEmail || ""}</p>
                    </div>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem onClick={() => navigate("/projects")} className="cursor-pointer">
                      <Layers className="w-4 h-4 mr-2" />
                      Projects Dashboard
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => navigate("/settings")} className="cursor-pointer">
                      <CreditCard className="w-4 h-4 mr-2" />
                      Billing & Settings
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem onClick={handleLogout} className="text-red-600 focus:text-red-600 cursor-pointer">
                      <LogOut className="w-4 h-4 mr-2" />
                      Sign Out
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </>
            ) : (
              <>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => navigate("/login")}
                  className="text-muted-foreground hover:text-foreground text-xs font-semibold px-4 rounded-full"
                >
                  Sign In
                </Button>
                <Button
                  size="sm"
                  onClick={() => navigate("/signup")}
                  className="bg-primary hover:bg-primary/95 text-primary-foreground font-bold text-xs px-5 rounded-full shadow-md shadow-primary/10 transition-transform hover:scale-[1.02]"
                >
                  Get Started
                </Button>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative container max-w-screen-2xl pt-20 pb-16 px-4 sm:px-8 text-center flex flex-col items-center justify-center z-10">
        {/* Decorative Badge */}
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-primary/10 border border-primary/20 text-xs font-medium text-primary mb-6 animate-pulse">
          <Sparkles className="w-3.5 h-3.5" />
          <span>Introducing v1.0 Production Release</span>
        </div>

        {/* Dynamic Title */}
        <h1 className="text-4xl sm:text-5xl md:text-6xl font-extrabold tracking-tight max-w-4xl leading-[1.1] mb-6">
          From Prompt to Production.
          <span className="block bg-gradient-to-r from-primary via-indigo-400 to-indigo-500 bg-clip-text text-transparent mt-1">
            Build and Deploy AI Apps Instantly
          </span>
        </h1>

        {/* Subtitle */}
        <p className="text-muted-foreground text-sm sm:text-base md:text-lg max-w-2xl leading-relaxed mb-10">
          The ultimate AI-powered workspace for creators. Describe your application idea in plain English, watch the code construct in real-time, and deploy to Kubernetes sandboxes with one click.
        </p>

        {/* Hero CTAs */}
        <div className="flex flex-col sm:flex-row gap-4 items-center justify-center mb-16">
          <Button
            size="lg"
            onClick={handleGetStarted}
            className="w-full sm:w-auto bg-gradient-to-r from-primary to-indigo-600 hover:from-primary/95 hover:to-indigo-600/95 text-white font-semibold shadow-xl shadow-primary/10 px-8 rounded-full h-12 gap-2 group transition-all"
          >
            {isAuth ? "Go to Dashboard" : "Get Started for Free"}
            <ArrowRight className="w-4 h-4 transition-transform group-hover:translate-x-1" />
          </Button>
          <Button
            variant="outline"
            size="lg"
            onClick={() => document.getElementById("features")?.scrollIntoView({ behavior: "smooth" })}
            className="w-full sm:w-auto border-border/60 hover:bg-muted/30 text-muted-foreground hover:text-foreground font-semibold px-8 rounded-full h-12"
          >
            Explore Features
          </Button>
        </div>

        {/* Screenshot Mockup Split Panel */}
        <div className="w-full max-w-5xl aspect-[16/10] bg-panel/40 border border-border/40 rounded-2xl p-2 sm:p-3 shadow-2xl relative overflow-hidden group">
          <div className="absolute inset-0 bg-gradient-to-t from-background via-transparent to-transparent z-10 opacity-40 pointer-events-none" />
          
          {/* Header controls of Mockup */}
          <div className="flex items-center justify-between px-3 pb-2 border-b border-border/40 shrink-0">
            <div className="flex gap-1.5">
              <div className="w-3 h-3 rounded-full bg-red-500/80" />
              <div className="w-3 h-3 rounded-full bg-yellow-500/80" />
              <div className="w-3 h-3 rounded-full bg-green-500/80" />
            </div>
            <div className="text-[11px] text-muted-foreground font-mono bg-muted/40 px-3 py-0.5 rounded border border-border/20">
              project-companion.io/workspace/1029
            </div>
            <div className="w-10" />
          </div>

          {/* Inside Split-View Mockup */}
          <div className="w-full h-full flex mt-2 bg-background/50 rounded-lg overflow-hidden border border-border/20 text-left">
            {/* Chat Pane */}
            <div className="w-[35%] border-r border-border/20 bg-panel/20 p-4 space-y-4 font-sans text-xs">
              <div className="p-3 bg-muted/30 rounded-lg border border-border/20 max-w-[85%]">
                <p className="font-semibold text-primary text-[10px] mb-1 uppercase tracking-wider">User Prompt</p>
                <p className="text-foreground/90">Build a responsive Kanban board with beautiful drag-and-drop cards and a dark-mode theme.</p>
              </div>
              <div className="p-3 bg-primary/5 rounded-lg border border-primary/10 max-w-[90%] self-end">
                <p className="font-semibold text-indigo-400 text-[10px] mb-1 uppercase tracking-wider">AI Assistant</p>
                <div className="flex items-center gap-1.5 text-muted-foreground font-mono text-[10px] mb-2">
                  <Terminal className="w-3 h-3" />
                  <span>creating kanban_component.tsx...</span>
                </div>
                <div className="w-full h-2 bg-muted rounded overflow-hidden">
                  <div className="h-full bg-primary w-2/3 animate-pulse" />
                </div>
              </div>
            </div>

            {/* Preview Iframe Pane */}
            <div className="flex-1 bg-muted/10 p-5 flex flex-col justify-between font-sans">
              <div className="flex justify-between items-center pb-3 border-b border-border/20">
                <div className="flex items-center gap-2">
                  <Globe className="w-4 h-4 text-emerald-400 animate-pulse" />
                  <span className="text-xs font-semibold text-emerald-400">Live Preview Sandbox</span>
                </div>
                <span className="text-[10px] bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded-full font-bold uppercase tracking-wider">
                  Active
                </span>
              </div>

              {/* Kanban Mock Visuals */}
              <div className="flex-1 py-8 flex gap-4 overflow-hidden">
                <div className="flex-1 bg-background/60 rounded-xl p-3 border border-border/30 flex flex-col gap-2">
                  <p className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">To Do</p>
                  <div className="h-10 bg-panel/60 rounded border border-border/20 p-2 text-[10px]">Create API models</div>
                  <div className="h-10 bg-panel/60 rounded border border-border/20 p-2 text-[10px]">Stripe webhooks</div>
                </div>
                <div className="flex-1 bg-background/60 rounded-xl p-3 border border-border/30 flex flex-col gap-2">
                  <p className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">In Progress</p>
                  <div className="h-10 border border-primary/20 bg-primary/5 rounded p-2 text-[10px] flex items-center justify-between">
                    <span>Pricing Grid</span>
                    <Zap className="w-3 h-3 text-primary animate-pulse" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Feature Bento Grid */}
      <section id="features" className="container max-w-screen-2xl py-24 px-4 sm:px-8 border-t border-border/40 relative z-10">
        <div className="text-center mb-16">
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight">Built for Unmatched Speed</h2>
          <p className="text-muted-foreground mt-3 max-w-lg mx-auto text-sm sm:text-base">
            No configuration, no local environment, no deployment friction. Simply create and scale.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {/* Card 1 */}
          <div className="p-6 rounded-2xl bg-panel/30 border border-border/40 hover:border-primary/40 hover:shadow-lg hover:shadow-primary/5 transition-all duration-300 group flex flex-col justify-between aspect-[16/11]">
            <div className="p-3 rounded-xl bg-primary/10 text-primary border border-primary/20 w-11 h-11 flex items-center justify-center shrink-0">
              <Terminal className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-lg group-hover:text-primary transition-colors mb-1.5">Collaborative Prompting</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Watch files automatically compile, format, and organize dynamically as the AI designs.
              </p>
            </div>
          </div>

          {/* Card 2 */}
          <div className="p-6 rounded-2xl bg-panel/30 border border-border/40 hover:border-indigo-500/40 hover:shadow-lg hover:shadow-indigo-500/5 transition-all duration-300 group flex flex-col justify-between aspect-[16/11]">
            <div className="p-3 rounded-xl bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 w-11 h-11 flex items-center justify-center shrink-0">
              <Globe className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-lg group-hover:text-indigo-400 transition-colors mb-1.5">Instant Live Sandbox</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Interact with the rendered live iframe preview matching modifications immediately.
              </p>
            </div>
          </div>

          {/* Card 3 */}
          <div className="p-6 rounded-2xl bg-panel/30 border border-border/40 hover:border-emerald-500/40 hover:shadow-lg hover:shadow-emerald-500/5 transition-all duration-300 group flex flex-col justify-between aspect-[16/11]">
            <div className="p-3 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 w-11 h-11 flex items-center justify-center shrink-0">
              <Cpu className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-lg group-hover:text-emerald-400 transition-colors mb-1.5">One-Click GKE Deploys</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Publish full-stack applications with built-in sandbox routing configurations.
              </p>
            </div>
          </div>

          {/* Card 4 */}
          <div className="p-6 rounded-2xl bg-panel/30 border border-border/40 hover:border-amber-500/40 hover:shadow-lg hover:shadow-amber-500/5 transition-all duration-300 group flex flex-col justify-between aspect-[16/11]">
            <div className="p-3 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20 w-11 h-11 flex items-center justify-center shrink-0">
              <Crown className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-lg group-hover:text-amber-400 transition-colors mb-1.5">Priority Core Sandboxes</h3>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Unlock high-priority builds, sandbox virtual cores, and Stripe-managed subscriptions.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Pricing Grid Section */}
      <section id="pricing" className="container max-w-screen-2xl py-24 px-4 sm:px-8 border-t border-border/40 relative z-10">
        <div className="text-center mb-16">
          <h2 className="text-3xl sm:text-4xl font-bold tracking-tight">Flexible SaaS Subscription Plans</h2>
          <p className="text-muted-foreground mt-3 max-w-lg mx-auto text-sm sm:text-base">
            Upgrade, manage, and scale your AI app projects. Cancel anytime.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          {/* Plan 1 */}
          <div className="p-6 rounded-2xl bg-panel/30 border border-border/40 hover:border-border transition-all flex flex-col justify-between min-h-[460px]">
            <div>
              <div className="mb-6">
                <h3 className="text-lg font-bold text-muted-foreground">Free Tier</h3>
                <p className="text-xs text-muted-foreground/80 mt-1">Get started building apps</p>
                <div className="mt-4 flex items-baseline">
                  <span className="text-4xl font-extrabold">$0</span>
                  <span className="text-muted-foreground text-xs ml-1">/ month</span>
                </div>
              </div>
              <div className="h-px bg-border/40 mb-6" />
              <ul className="space-y-3.5 text-xs text-muted-foreground text-left">
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-green-500 shrink-0" />
                  <span>Up to 3 Active Projects</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-green-500 shrink-0" />
                  <span>Basic AI Code Generation</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-green-500 shrink-0" />
                  <span>Standard Sandbox Deployment</span>
                </li>
                <li className="flex items-center gap-2.5 text-muted-foreground/40">
                  <Check className="w-4 h-4 text-muted-foreground/20 shrink-0" />
                  <span className="line-through">Priority Build Queue</span>
                </li>
                <li className="flex items-center gap-2.5 text-muted-foreground/40">
                  <Check className="w-4 h-4 text-muted-foreground/20 shrink-0" />
                  <span className="line-through">Unlimited Collaboration</span>
                </li>
              </ul>
            </div>
            <Button variant="outline" onClick={handleGetStarted} className="w-full mt-8 rounded-xl font-semibold">
              {isAuth ? "Go to Dashboard" : "Start Free Plan"}
            </Button>
          </div>

          {/* Plan 2 */}
          <div className="p-6 rounded-2xl bg-primary/5 border border-primary/40 hover:border-primary/80 shadow-xl shadow-primary/5 relative flex flex-col justify-between min-h-[460px] overflow-hidden">
            <div className="absolute top-0 right-0 bg-primary text-primary-foreground text-[10px] font-bold uppercase tracking-wider px-3 py-1 rounded-bl-lg">
              Recommended
            </div>
            <div>
              <div className="mb-6">
                <h3 className="text-lg font-bold text-primary flex items-center gap-1.5">
                  <Crown className="w-4 h-4 text-primary animate-pulse" /> Pro Plan
                </h3>
                <p className="text-xs text-muted-foreground/80 mt-1">For advanced creators and builders</p>
                <div className="mt-4 flex items-baseline">
                  <span className="text-4xl font-extrabold text-primary">$15</span>
                  <span className="text-muted-foreground text-xs ml-1">/ month</span>
                </div>
              </div>
              <div className="h-px bg-primary/20 mb-6" />
              <ul className="space-y-3.5 text-xs text-left">
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-primary shrink-0" />
                  <span>Unlimited AI App Projects</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-primary shrink-0" />
                  <span>Pro Smart AI Generations</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-primary shrink-0" />
                  <span>Fast Priority Sandbox Builds</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-primary shrink-0" />
                  <span>Premium Real-Time Sandbox Previews</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-primary shrink-0" />
                  <span>Advanced Project Collaboration</span>
                </li>
              </ul>
            </div>
            <Button
              onClick={() => handlePricingClick(1)}
              className="w-full mt-8 bg-primary hover:bg-primary/95 text-primary-foreground font-bold shadow-md shadow-primary/20 rounded-xl"
            >
              {isAuth ? "Upgrade Now" : "Subscribe to Pro"}
            </Button>
          </div>

          {/* Plan 3 */}
          <div className="p-6 rounded-2xl bg-indigo-500/5 border border-indigo-500/30 hover:border-indigo-500/80 relative flex flex-col justify-between min-h-[460px] overflow-hidden">
            <div className="absolute top-0 right-0 bg-indigo-500 text-white text-[10px] font-bold uppercase tracking-wider px-3 py-1 rounded-bl-lg">
              Team Elite
            </div>
            <div>
              <div className="mb-6">
                <h3 className="text-lg font-bold text-indigo-400 flex items-center gap-1.5">
                  <Crown className="w-4 h-4 text-indigo-400" /> Business Plan
                </h3>
                <p className="text-xs text-muted-foreground/80 mt-1">For startups and agency scales</p>
                <div className="mt-4 flex items-baseline">
                  <span className="text-4xl font-extrabold text-indigo-400">$49</span>
                  <span className="text-muted-foreground text-xs ml-1">/ month</span>
                </div>
              </div>
              <div className="h-px bg-indigo-500/20 mb-6" />
              <ul className="space-y-3.5 text-xs text-left">
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                  <span>Everything in Pro Plan</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                  <span>Unlimited Collaborators & Teams</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                  <span>White-labeled Custom Sandboxes</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                  <span>Dedicated Sandbox Resource Allocation</span>
                </li>
                <li className="flex items-center gap-2.5">
                  <Check className="w-4 h-4 text-indigo-400 shrink-0" />
                  <span>24/7 Priority SLA Technical Support</span>
                </li>
              </ul>
            </div>
            <Button
              onClick={() => handlePricingClick(2)}
              className="w-full mt-8 bg-indigo-500 hover:bg-indigo-500/95 text-white font-bold shadow-md shadow-indigo-500/20 rounded-xl"
            >
              {isAuth ? "Upgrade Now" : "Subscribe to Business"}
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border/40 py-10 bg-panel/10 relative z-10 text-center text-xs text-muted-foreground">
        <div className="container max-w-screen-2xl px-4 sm:px-8 space-y-4">
          <div className="flex items-center justify-center gap-2 font-bold text-sm text-foreground">
            <Sparkles className="w-4 h-4 text-primary" />
            <span>Project Companion</span>
          </div>
          <p>© {new Date().getFullYear()} Project Companion. Built for next-generation software builders.</p>
        </div>
      </footer>
    </div>
  );
}
